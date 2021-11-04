counter=0
while true; do
   curl --location --request GET 'http://localhost:8080/cancel/cancel-'$counter &
   #sleep 0.005
   kill $(ps -ef | grep 'cancel-'$counter | head -n 1 | awk '{print $2}') &
   #sleep 0.005
   if [[ $((counter % 20)) -eq 0 ]]; then
      curl --location --request GET 'http://localhost:8080/cancel/complete-'$counter
   elif [[ $counter -gt 2000 ]]; then
     echo exit
     exit 1
   fi
   counter=$((counter+1))
done


