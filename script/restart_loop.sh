while true
do
    java -Xmx2G -jar forge-1.14.4-28.1.96.jar
	read -r should_restart < ./auto_restart/restart
	if [ $should_restart == "0" ]
	then
		break
	fi
    echo 'Willst Du den Server komplett stoppen, drücke STRG-C, \nbevor die Zeit bei 1 ist!'
    echo "Rebooting in:"
    for i in 5 4 3 2 1
    do
        echo "$i..."
        sleep 1
    done
    echo 'Server neustart!'
done