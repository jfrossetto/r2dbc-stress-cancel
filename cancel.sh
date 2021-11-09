counter=0
#while true; do
while [[ $counter -lt 2000 ]]; do
   curl --location --request GET 'http://localhost:8080/cancel/cancel-'$counter &
   sleep 0.003
   kill $(ps -ef | grep 'cancel-'$counter | head -n 1 | awk '{print $2}') &
   #sleep 0.005
   #   echo $counter
   #if [[ $((counter % 10)) -eq 0 ]]; then
   #   curl --location --request GET 'http://localhost:8080/cancel/complete-'$counter
   #else
      curl --location --request GET 'http://localhost:8080/cancel/complete-'$counter
   #fi
   counter=$((counter+1))
done


