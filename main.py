"""

This software is licensed under the Modified BSD License:

Copyright (c) 2013, Sigve Tjora
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:
    * Redistributions of source code must retain the above copyright
      notice, this list of conditions and the following disclaimer.
    * Redistributions in binary form must reproduce the above copyright
      notice, this list of conditions and the following disclaimer in the
      documentation and/or other materials provided with the distribution.
    * Neither the name of the <organization> nor the
      names of its contributors may be used to endorse or promote products
      derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
"""

import OSC
import time
import threading
import queue
from collections import namedtuple
from x32parameters import get_settings
import json
import sys
import math

ReceivedMessage = namedtuple("ReceivedMessage", "address, tags, data, client_address")

def answers_to_queue_thread(server, queue):

    def add_to_queue(addr, tags, data, client_address):
        msg = ReceivedMessage(address=addr, 
                              tags=tags, 
                              data=data, 
                              client_address = client_address)
        queue.put(msg)

    server.addMsgHandler("default", add_to_queue)    
    thread = threading.Thread(target=server.serve_forever)
    thread.daemon = True
    thread.start()
    return thread

setting_paths = get_settings()

class TimeoutError(Exception):
    pass

class BehringerX32(object):
    def __init__(self, x32_address, server_port, verbose, timeout=10, behringer_port=10023):
        self._verbose = verbose
        self._timeout = timeout
        self._server = OSC.OSCServer(("", server_port))
        self._client = OSC.OSCClient(server=self._server) #This makes sure that client and server uses same socket. This has to be this way, as the X32 sends notifications back to same port as the /xremote message came from
        
        self._client.connect((x32_address, behringer_port))
        
        self._input_queue = queue.Queue()
        self._listener_thread = answers_to_queue_thread(self._server, queue=self._input_queue)

        self._saved = []

    def __del__(self):
        self._server.close()
        self._client.close()

    def ping(self):
        self.get_value(path="/info")

    def send_and_store(self, osc: OSC.OSCMessage):
        self._saved.append(OSC.OSCMessage(osc.address, *self.get_value(osc.address)))

        self._client.send(osc)

    def restore_all(self):
        for saved in reversed(self._saved):
            print(saved)
            self._client.send(saved)
            time.sleep(0.001)

        self._saved.clear()
    
    def get_value(self, path):
        while True:
            try:
                self._input_queue.get_nowait()
            except queue.Empty:
                break            
        self._client.send(OSC.OSCMessage(path))
        return self._input_queue.get(timeout=self._timeout).data

    def set_value(self, path, value, readback=True):
        self._client.send(OSC.OSCMessage(path, value))
        if readback:
            start_time = time.time()
            while True:
                read_back_value = self.get_value(path)
                #Special case for nans
                if len(value) == 1 and len(read_back_value)==1:
                    if type(value[0]) is float and math.isnan(value[0]) and math.isnan(read_back_value[0]):
                        break
                if read_back_value == value:
                    break
                if time.time() - start_time > self._timeout:
                    raise TimeoutError("Timeout while readback of path %s, value=%s, read_back_value=%s" % (path, value, read_back_value)) 
                time.sleep(0.0001)
                
    def get_state(self):        
        state = {}
        for index, path in enumerate(setting_paths):
            if self._verbose and index % 100 == 0:
                print("Reading parameter %d of %d from x32" % (index, len(setting_paths)))
            value = self.get_value(path)
            assert len(value) == 1
            state[path] = value[0]
        return state
    
    def set_state(self, state):
        """Set state will first set all faders to 0, then load all values except for faders and at the end will it restore the faders.
        
        This is to avoid feedbacks/high volume during restore, if some in between setting would cause problems.
        """
        fader_keys = sorted(key for key in state if key.endswith("fader"))
        parameters = [(key, 0.0) for key in fader_keys]
        parameters.extend((key, state[key]) for key in sorted(state.iterkeys()) if key not in fader_keys)
        parameters.extend((key, state[key]) for key in fader_keys)
        
        for index, my_tuple in enumerate(parameters):
            key, value = my_tuple
            if self._verbose and index % 100 == 0:
                print("Writing parameter %d of %d to x32" % (index, len(state)))
            self.set_value(path=key, value=[value], readback=True)
        return
            
    def save_state_to_file(self, outputfile, state):
        my_dict = {"x32_state": state,
                   }
        json.dump(my_dict, outputfile, sort_keys=True, indent=4)

    def read_state_from_file(self, inputfile):
        my_dict = json.load(inputfile)
        return my_dict["x32_state"]



usage = """This is a utility to load or save the settings of a Behringer X32 mixing desk. All document settings are loaded/stored, and some undocumented.

It is possible to edit the state-files (they are in JSON-format) to delete properties which one does not want to restore.

Beware: This is alpha software, fileformats may change in future.
"""
    
if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(description=usage)
    parser.add_argument('--address', default="192.168.0.20",                      
                        help='name/ip-address of Behringer X32 mixing desk') 
    parser.add_argument('--port', default = 10300,                        
                        help='UDP-port to open on this machine.')

    args = parser.parse_args()

    mixer = BehringerX32(x32_address=args.address, server_port=int(args.port), verbose=True)
    mixer.ping()

    mixer.set_value("/bus/07/mix/fader", [0])

    run = True
    n = 16
    t = 0
    while run:
        t += 0.05
        for i in range(1, n + 1):
            mixer.set_value(f'/ch/{i // 10}{i % 10}/mix/fader', [0.5 * math.sin(t + 0.3 * i) + 0.5], False)

        time.sleep(0.02)
