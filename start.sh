# !/bin/bash

nohup java -jar target/PatternMiningDemo-0.0.1-SNAPSHOT.jar &
cd frontend
nohup npm start &
cd .. 
echo "ok"
lsof -i:23333
lsof -i:23334

