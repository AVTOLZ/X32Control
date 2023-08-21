import time

import OSC
from main import BehringerX32

if __name__ == '__main__':
    import argparse

    parser = argparse.ArgumentParser(description="")
    parser.add_argument('--address', default="192.168.0.20",
                        help='name/ip-address of Behringer X32 mixing desk')
    parser.add_argument('--port', default=10300,
                        help='UDP-port to open on this machine.')

    args = parser.parse_args()

    mixer = BehringerX32(x32_address=args.address, server_port=int(args.port), verbose=True)
    mixer.ping()

    while True:
        command = input("Comamnd: ")
        # state = mixer.get_state()

        if command == "fakelock":
            osc = OSC.OSCMessage("/-stat/lock")
            osc.append(1)

            current_screen = mixer.get_value("/-stat/screen/screen")

            osc2 = OSC.OSCMessage("/-stat/screen/screen")
            osc2.append(current_screen)

            mixer.send_and_store(osc)
            time.sleep(0.0001)
            mixer.send_and_store(osc2)

            time.sleep(0.0001)
            try:
                for i in range(1, 17):
                    osc = OSC.OSCMessage(f"/ch/{i // 10}{i % 10}/config/color")
                    osc.append(3)
                    mixer.send_and_store(osc)
                    print(osc)
                    time.sleep(0.0001)

                current_screen = mixer.get_value("/-stat/screen/screen")
                osc2 = OSC.OSCMessage("/-stat/screen/screen")
                osc2.append(current_screen)

                mixer.send_and_store(osc)

                time.sleep(0.0001)
                mixer.send_and_store(osc2)

                n = 0
                while True:
                    for i in range(1, 17):
                        n += 1
                        osc = OSC.OSCMessage(f"/ch/{i // 10}{i % 10}/config/color")
                        osc.append((n % 7) + 1)
                        mixer._client.send(osc)
                        print(osc)
                        time.sleep(0.001)
                    n += 1

            except KeyboardInterrupt:
                pass

            continue

        elif command == "unlock":
            osc = OSC.OSCMessage("/-stat/lock")
            osc.append(0)

            mixer.send_and_store(osc)
            # mixer.set_state(state)
            continue

        elif command == "restore":
            mixer.restore_all()

            continue

        elif command == "exit":
            break

        osc = OSC.OSCMessage(command)
        argument = input("Arguments (leave empty to send none): ")
        arg_type = input("argument type: ")

        if argument != "":
            exec("arg = " + arg_type + "(" + argument + ")")
            osc.append(arg)

        print(osc)

        mixer._client.send(osc)
