Fix scheduled, empty server and low TPS restarts not working at all, if the server needs more than 60 seconds to start (the restart timer stopped after an error)
Fix the restart being triggered again every second during the restart minute
Fix an error being logged every second while the server is stopping
