# Constant_Vigilance
It's an Android Service that scans for potentially harmful applications. 

The idea was to build a database of OWASP's most dangerous mobile malwares and detect those, but it turns out detecting malware like that is easier said than done so for the time being, we've used Google's SafetyNet API to do the heavy lifting.

There was also some issue with the notifications getting updated, so the current jank solution is to let it sleep for the specified time, then close it, then fire it again. This will be fixed as soon as I get some time on my hands.

