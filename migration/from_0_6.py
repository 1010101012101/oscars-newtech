#!/usr/bin/env python
# encoding: utf-8
# coding: interpy

import MySQLdb as mdb
import json
import yaml

stream = open('ifce-addrs.yaml', 'r')
addrs = yaml.load(stream)

# connect and make cursors
rdb = mdb.connect(host="localhost",
                  user="root",
                  db="rm")

pdb = mdb.connect(host="localhost",
                  user="root",
                  db="eomplspss")


rcur = rdb.cursor(mdb.cursors.DictCursor)
pcur = pdb.cursor(mdb.cursors.DictCursor)

# grab all active reservations
rcur.execute("SELECT * FROM reservations WHERE status = 'ACTIVE'")
results = {}

# print the first and second columns
for res in rcur.fetchall():
    # make another cursor for deeper queries
    cur = rdb.cursor(mdb.cursors.DictCursor)

    gri = res['globalReservationId']
    # skip all the NSI ones
    if res['login'] == 'nsi_aggr':
        continue

    # strip production string from description, move it to flag
    is_prod = False
    description = res['description']
    if "[PRODUCTION CIRCUIT]" in res['description']:
        is_prod = True
        description = res['description'].replace("[PRODUCTION CIRCUIT] ", '')

    # make data structure
    out = {
        'gri': gri,
        'misc': {
            'description': description,
            'production': is_prod,
            'user': res['login'],
            'policing': 'soft',
            'protection': 'none',
            'applyQos': True,
        },
        'schedule': {
            'start': res['startTime'],
            'end': res['endTime']
        },
        'mbps': None,
        'cmp': {
            'pipe': {

            },
            'junctions': [

            ],
            'fixtures': []
        },
        'pss': {
            'res': {
                'vplsId': [],
                'resources': []
            },
            'config': []
        }
    }

    # all we need from stdConstraints is the pathId and bandwidth
    cur.execute("SELECT * FROM stdConstraints WHERE constraintType = 'reserved' AND reservationId = %s" % res['id'])
    pathId = -1
    for stdConstraint in cur.fetchall():
        pathId = stdConstraint['pathId']
        out['mbps'] = stdConstraint['bandwidth'] / 1000000

    if pathId == -1:
        print "no path id for %s" % gri
        exit(1)

    # walk the pathElems and make the fixtures, junction(s) and pipe
    aDevice = None
    aPort = None
    aVlan = None
    zDevice = None
    zPort = None
    zVlan = None
    cur.execute("SELECT * FROM pathElems WHERE pathId = %s ORDER BY seqNumber" % pathId)
    hops = []
    firstHop = True
    prevDevice = None
    pathElems = cur.fetchall()
#    print json.dumps(pathElems, indent=2)
    prevDevice = None
    for pathElem in pathElems:
        urn = pathElem['urn']
        addr = ''
        if urn in addrs:
            addr = addrs[urn]

        urn = urn.replace('urn:ogf:network:domain=es.net:node=', '')
        urn = urn.replace('port=', '')
        urn = urn.replace('link=', '')
        parts = urn.split(':')

        # check if we have any associated pathElemParams and grab the VLAN ids from there.
        ncur = rdb.cursor(mdb.cursors.DictCursor)
        ncur.execute("SELECT * FROM pathElemParams WHERE type = 'suggestedVlan' AND pathElemId = %s" % pathElem['id'])
        for pep in ncur.fetchall():
            if firstHop:
                aVlan = pep['value']
            zVlan = pep['value']

        if firstHop:
            aDevice = parts[0]
            aPort = parts[1]
        zDevice = parts[0]
        zPort = parts[1]

        if prevDevice == parts[0]:
            hops.append({
                'device': parts[0],
                'port': '',
                'addr': ''
            })

        prevDevice = parts[0]

        hops.append({
            'device': parts[0],
            'port': parts[1],
            'addr': addr
        })

        firstHop = False

    # slice hops to skip the first and last elements
    hops = hops[1:-1]

    if aDevice == zDevice:
        out['cmp']['junctions'] = [aDevice]
        out['cmp']['pipe'] = []
    else:
        out['cmp']['junctions'] = [aDevice, zDevice]
        out['cmp']['pipe'] = hops

    out['cmp']['fixtures'] = [
        {
            'junction': aDevice,
            'port': aPort,
            'vlan': aVlan
        },
        {
            'junction': zDevice,
            'port': zPort,
            'vlan': zVlan
        }
    ]

    # set flags according to optConstraints

    cur.execute("SELECT * FROM optConstraints WHERE reservationId = %s" % res['id'])
    for optional in cur.fetchall():
        if optional['keyName'] == 'policing':
            out['misc']['policing'] = optional['value']
        if optional['keyName'] == 'protection':
            out['misc']['protection'] = optional['value']
        if optional['keyName'] == 'apply-qos':
            out['misc']['applyQos'] = optional['value']

    # grab configs for build, dismantle

    pcur.execute("SELECT * FROM config WHERE phase = 'SETUP' AND gri = '%s'" % res['globalReservationId'])
    for config in pcur.fetchall():
        device = config['deviceId']
        entry = {
            'device': device,
            'phase': 'BUILD',
            'config': config['config']
        }
        out['pss']['config'].append(entry)

    pcur.execute("SELECT * FROM config WHERE phase = 'TEARDOWN' AND gri = '%s'" % res['globalReservationId'])
    for config in pcur.fetchall():
        device = config['deviceId']
        entry = {
            'device': device,
            'phase': 'DISMANTLE',
            'config': config['config']
        }
        out['pss']['config'].append(entry)

    # grab those convoluted PSS resources
    pcur.execute("SELECT * FROM srl WHERE gri = '%s'" % gri)
    for srl in pcur.fetchall():
        if srl['scope'] == 'es.net:vpls':
            out['pss']['res']['vplsId'].append(srl['resource'])
        elif gri+':vpls' in srl['scope']:
            continue
        else:
            parts = srl['scope'].split(':')
            device = parts[0]
            what = parts[1]
            entry = {
                'what': what,
                'resource': srl['resource'],
                'device': device
            }
            out['pss']['res']['resources'].append(entry)

# add new data, end of loop
    results[gri] = out


# now grab the rest of the PSS resources
# these ones append the device name to the gri column and are not caught by the previous query.
pcur.execute("SELECT * FROM srl WHERE scope = 'es.net:vpls-loopback'")
for srl in pcur.fetchall():
    parts = srl['gri'].split(':')
    gri = parts[0]
    device = parts[1]
    if gri in results:
        entry = {
            'what': 'loopback',
            'resource': srl['resource'],
            'device': device
        }
        results[gri]['pss']['res']['resources'].append(entry)

# dump JSON output & exit

final = []
for gri in results:
    final.append(results[gri])

print json.dumps(final, indent=2)
