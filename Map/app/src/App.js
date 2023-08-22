import React, { useEffect, useState } from "react";
import useWebSocket from "react-use-websocket";

// ... [The rest of your components and imports]

export const FactoryMap = () => {
  const [socketUrl, setSocketUrl] = useState("ws://localhost:8080/mapupdates");
  
  // Instead of just saving the current version, we'll save all versions in an array.
  const [astarArrayVersions, setAstarArrayVersions] = useState([]);
  const [currentVersionIndex, setCurrentVersionIndex] = useState(0); // Index for current version

  const {
    lastMessage,
    readyState,
  } = useWebSocket(socketUrl);

  useEffect(() => {
    if (lastMessage !== null) {
      const newData = JSON.parse(lastMessage.data);
      setAstarArrayVersions(prev => [...prev, newData.astarArray]);
      setCurrentVersionIndex(prev => prev + 1);
    }
    
  }, [lastMessage]);

  return (
    <div>
      <div>
        <h3>Factory Map</h3>
        {astarArrayVersions[currentVersionIndex] && <pre>{astarArrayVersions[currentVersionIndex]}</pre>}
        
        {/* Display the slider only when there's more than 1 version */}
        {astarArrayVersions.length > 1 && (
          <div>
            <input 
              type="range" 
              min="0" 
              max={astarArrayVersions.length - 1} 
              value={currentVersionIndex} 
              onChange={e => setCurrentVersionIndex(Number(e.target.value))} 
            />
            <span>Version: {currentVersionIndex + 1}/{astarArrayVersions.length}</span>
          </div>
        )}
      </div>
    </div>
  );
};

// ... [Rest of the code for App component and other logic]

function App() {
  return (
    <div className="App">
    <div className='Task'>Hola</div>
      <FactoryMap/>
    </div>
  );
}

export default App;
