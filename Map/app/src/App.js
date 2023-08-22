import logo from './logo.svg';
import './App.css';

import React, { useEffect, useState } from "react";
import useWebSocket from "react-use-websocket";

export const FactoryMap = () => {
  const [socketUrl, setSocketUrl] = useState("ws://localhost:8080/mapupdates");
  const [data, setData] = useState({
    map: null,
    astarArray: null,
    packageTaskQueue: null,
  });

  const {
    lastMessage,
    readyState,
  } = useWebSocket(socketUrl);

  useEffect(() => {
    if (lastMessage !== null) {
      const newData = JSON.parse(lastMessage.data);
      setData(newData);
    }
  }, [lastMessage]);

  return (
    <div>
      <div>
        <h3>Factory Map</h3>
        {data.map && Object.keys(data.map).map((rowKey, rowIndex) => (
          <div key={rowIndex}>
            {Object.keys(data.map[rowKey]).map((colKey, colIndex) => (
              <div key={colIndex}>
                Cell ({rowKey},{colKey}): {data.map[rowKey][colKey]}
              </div>
            ))}
          </div>
        ))}
      </div>

      <div>
      <h3>A* Array</h3>

      {/* <pre>{JSON.stringify(data.astarArray, null, 2)}</pre> */}

      
        {data.astarArray && data.astarArray.map((row, rowIndex) => (
          <div key={rowIndex} style={{ display: 'flex' }}>
            {row.map((value, colIndex) => (
              <div 
                key={colIndex}
                style={{
                  width: '20px',
                  height: '20px',
                  backgroundColor: value === 2? 'black' : 'white', // change based on your logic
                  border: '1px solid gray'
                }} 
              />
            ))}
          </div>
        ))}
      </div>

      <div>
        <h3>Package Task Queue</h3>
        {data.packageTaskQueue && data.packageTaskQueue.map((task, index) => (
          <div 
            key={index} 
            style={{
              backgroundColor: "brown",
              padding: '10px',
              margin: '5px',
              color: 'white',
              borderRadius: '5px',
              maxWidth: '300px'
            }}>
            ID: {task.id} <br />
            Origin: {JSON.stringify(task.origin)} <br />
            Destination: {JSON.stringify(task.destination)} <br />
            {/* Weight: {task.package.weight} */}
          </div>
        ))}
      </div>
    </div>
  );
};

function App() {
  return (
    <div className="App">
      <p>Hi</p>
      <FactoryMap/>
    </div>
  );
}

export default App;
