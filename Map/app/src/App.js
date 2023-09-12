import React, { useEffect, useState } from "react";
import useWebSocket from "react-use-websocket";
import Box from '@mui/material/Box';
import './App.css';
import Cell from "./Componets/Cell";
import isEqual from 'lodash/isEqual';


import { createTheme } from '@mui/material/styles';
import { Typography } from "@mui/material";
import { ThemeProvider } from '@mui/material/styles';




const theme = createTheme({
  palette: {
    primary: {
      main: '#4FC3F7',
    },
    background:{
      main:"#FAFAFA",
      second:"#E0E0E0",
    },
    robot:{
      main:"#607D8B"
    }
  },
  typography: {
    h1: {
      fontSize: '2rem',
      fontWeight: 500,
    },
  },

})



const ObjectViewer = ({ data }) => {
  return (
    <Box sx={{ padding: '16px', border: '1px solid #ccc', marginBottom: '16px' }}>
      <Typography variant="body1"><strong>ID:</strong> {data.id}</Typography>
      <Typography variant="body1"><strong>Origin:</strong> [{data.origin.join(", ")}]</Typography>
      <Typography variant="body1"><strong>Destination:</strong> [{data.destination.join(", ")}]</Typography>
       <Typography variant="body1"><strong>Package Weight:</strong> {data.pkg.weight}</Typography> 
    </Box>
  );
};


const Robots = ({ grid }) => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', width: '60%', height: '100%' }}>
      {grid.map((row, rowIndex) => (
        <Box key={rowIndex} sx={{ display: 'flex', flexGrow: 1 }}>
          {row.map((cell, cellIndex) => (
            <Box
              key={cellIndex}
              sx={{
                flexGrow: 1,
                border: '1px solid #ccc',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                minHeight: '60px',
                maxWidth: '100px'
              }}
            >
              <Cell value={cell} rowIndex={rowIndex} cellIndex={cellIndex} />
            </Box>
          ))}
        </Box>
      ))}
    </Box>
  );
};


export const FactoryMap = () => {
  const [socketUrl, setSocketUrl] = useState("ws://localhost:4000/mapupdates");

  const [astarArrayVersions, setAstarArrayVersions] = useState([]);
  const [currentVersionIndex, setCurrentVersionIndex] = useState(0); // Index for current version

  const [taskVersions, setTaskVersions] = useState([]);
  const [currentTaskVersionIndex, setCurrentTaskVersionIndex] = useState(0);

  const { lastMessage, readyState } = useWebSocket(socketUrl);

  useEffect(() => {
    if (lastMessage !== null) {
      const newData = JSON.parse(lastMessage.data);

      // Update task versions
      if (taskVersions.length === 0 || 
        (taskVersions.length > 0 && 
        !isEqual(taskVersions[taskVersions.length - 1], newData.packageTaskQueue))) {
          setTaskVersions(prev => [...prev, newData.packageTaskQueue]);
          setCurrentTaskVersionIndex(prev => prev + 1);
      }

      // Update astarArrayVersions
      if (astarArrayVersions.length === 0 || 
         (astarArrayVersions.length > 0 && 
         !isEqual(astarArrayVersions[astarArrayVersions.length - 1], newData.astarArray))) {
          setAstarArrayVersions(prev => [...prev, newData.astarArray]);
          setCurrentVersionIndex(prev => prev + 1);
      }
    }
  }, [lastMessage, astarArrayVersions, taskVersions]);

  return (
    <div style={{width:"100%", height:"100%" , flexDirection:"column"}}>

      <div style={{display:"flex", width:"10%", flexDirection:"row"}}>
        {/* Update the task mapping to use the current version */}
        {taskVersions[currentTaskVersionIndex] && taskVersions[currentTaskVersionIndex].map((object, index) => (
          <ObjectViewer key={index} data={object} />
        ))}
      </div>

      {/* Add the slider for task versions */}
      {taskVersions.length > 1 && (
        <div style={{width:"100%", flexDirection:"row", marginTop: "20px"}}>
          <input
            type="range"
            min="0"
            max={taskVersions.length - 1}
            value={currentTaskVersionIndex}
            onChange={e => setCurrentTaskVersionIndex(Number(e.target.value))}
          />
          <span>Task Version: {currentTaskVersionIndex + 1}/{taskVersions.length}</span>
        </div>
      )}

      <div style={{display:"flex", width:"100%"}}>
        {astarArrayVersions.length > 1 && (
          <div style={{display:"flex", width:"100%",height:"50%" , flexDirection:"column-reverse"}}>
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
              
            { currentVersionIndex>=0? <Robots grid={astarArrayVersions[currentVersionIndex-1>0?currentVersionIndex-1:0]}/>  : null }
          </div>
        )}
      </div>
    </div>
  );
};


// ... [Rest of the code for App component and other logic]

function App() {

  const grid = [
    [100, 1, 0, 0, 0],
    [0, 0, 0, 0, 0],
    [1, 0, 1, 1, 0],
    [1, 0, 1, 1, 0],
    [1, 0, 5, 1, 0],
    [1, 0, 1, 1, 0],
    [1, 0, 1, 1, 0],
    [1, 0, 1, 1, 0],
    [1, 0, 1, 1, 0],
    [1, 0, 1, 1, 0],
  ];
  


  return (
    <div className="App">

      <ThemeProvider theme={theme}>

      <Box sx={
      {
       backgroundColor: theme.palette.background.main,
       height:"100vh",
       display:"flex",
       flexDirection: "column",

      }
    }>



      <Typography   variant="h1" style={{ color: theme.palette.primary.main }} >Robot Coalition</Typography>


      

      <Box  sx={
      {
       flexDirection: "row",
       display :"flex",
       flex:"1",
      }
    }>



        <FactoryMap></FactoryMap>


      </Box>
       
     


      </Box>
       
      </ThemeProvider>



    </div>
  );
}

export default App;
