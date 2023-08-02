import logo from './logo.svg';
import './App.css';


import React, { useEffect, useState } from "react";
import useWebSocket from "react-use-websocket";

export const FactoryMap = () => {
  const [socketUrl, setSocketUrl] = useState("ws://localhost:8080/mapupdates"); // Set your server url
  const [factoryMap, setFactoryMap] = useState(null);

  const {
    lastMessage,
    readyState,
  } = useWebSocket(socketUrl);

  useEffect(() => {
    if (lastMessage !== null) {
      const updatedMap = JSON.parse(lastMessage.data);
      setFactoryMap(updatedMap);
    }
  }, [lastMessage]);
  //console.log(lastMessage);

  return (
    <div>
      {factoryMap && Object.keys(factoryMap).map((rowKey, rowIndex) => (
        <div key={rowIndex}>
          {Object.keys(factoryMap[rowKey]).map((colKey, colIndex) => (
            <div key={colIndex}>
              Cell ({rowKey},{colKey}): {factoryMap[rowKey][colKey]}
            </div>
          ))}
        </div>
      ))}
    </div>
  );
};



function App() {
  return (
    <div className="App">
    <p> Hi</p>
      <FactoryMap/>
    </div>
  );
}

export default App;
