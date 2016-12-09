#!/usr/bin/python

# Value change threshold for communicating it
THRESHOLD   = 20
MININTERVAL = 1.0

# Address for the data collector servlet
COLLECTOR = "magi.virtuallypreinstalled.com"
COLLECTORPORT = 80
COLLECTORPATH = "/analprobe/collect"
PROBEID = 0
SERIALDEV = '/dev/ttyACM%d'

##########################################################
import serial,sys,re,httplib,urllib,datetime,datetime,time
from serial.serialutil import SerialException

# The previously communicated value
oldvalue  = -100
oldtime   = None

# The previous measurement
prevvalue = -1

##########################################################
# Log

def logWrite(str):
    timestamp = datetime.datetime.today().isoformat()
    print "%s %s" % (timestamp, str)
    sys.stdout.flush()

##########################################################
# Sends a data value to the collector

def communicate(value):
    logWrite("Sending %d" % (value))
    params = urllib.urlencode({"probe": PROBEID, "value": value})
    headers = {"Content-type": "application/x-www-form-urlencoded",
               "Accept": "text/plain"}

    connectionfails = 0
    while 1:
        try:
            conn = httplib.HTTPConnection(COLLECTOR, COLLECTORPORT, timeout=10)
            conn.request("POST", COLLECTORPATH, params, headers)
            conn.close()
            if connectionfails > 0:
                logWrite("Collector connection re-established")
            break
        except Exception as e:
            if connectionfails == 0:
                logWrite("Collector connection failed, retrying until successful...")
            connectionfails += 1
            time.sleep(10)
    
##########################################################
# Main

def serialOpen():
    serialfails = 0

    while 1:
        for devno in xrange(0,10):
            try:
                ser = serial.Serial(SERIALDEV % (devno), 9600, timeout=1)
                logWrite("Serial device %s opened" % (SERIALDEV % (devno)))
                return ser
            except SerialException as e:
                time.sleep(1)
        if serialfails == 0:
            logWrite("Could not open serial device, waiting...")
        serialfails += 1

# Open initially
ser = serialOpen()

while 1:
    try:
        line = ser.readline().strip()
        m = re.match(r'AIN[0-9]?=([0-9]+)', line)
        if m:
            value = int(m.group(1))
        
            # Check if more than minimum time has passed since last measurement
            curtime = datetime.datetime.today()
            diffsecs = 999
            if oldtime != None:
                diff = (curtime - oldtime)
                diffsecs = diff.seconds + diff.microseconds / 1000000.0

            if abs(value - oldvalue) >= THRESHOLD and diffsecs > MININTERVAL:
                logWrite("Analog input changed to %d " % (value))
                if prevvalue >= 0 and diffsecs > 2*MININTERVAL:
                    communicate(prevvalue)
                communicate(value)
                oldvalue = value
                oldtime = curtime
            prevvalue = value
    except SerialException as e:
        logWrite("Lost serial connection.")
        time.sleep(3)
        ser = serialOpen()
#    except OSError as e:
#        logWrite("OS Error, probably a serial problem.")
#        time.sleep(3)
#        ser = serialOpen()
