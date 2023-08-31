import React, { useEffect, useState } from "react";
import useWebSocket from "react-use-websocket";
import SmartToyIcon from '@mui/icons-material/SmartToy';
import Box from '@mui/material/Box';
import './App.css';
import Cell from "./Componets/Cell";


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


const Robots = ({ grid }) => {
    return (
      <Box sx={{ display: 'flex', flexDirection: 'column', height: '90%', width:"75%" }}>
        {grid.map((row, rowIndex) => (
          <Box key={rowIndex} sx={{ display: 'flex', flex: 1 }}>
            {row.map((cell, cellIndex) => (
              <Box
                key={cellIndex}
                sx={{
                  flex: 1,
                  border: '1px solid #ccc',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center'
                }}
              >
              
                <Cell value={cell}>

                </Cell>
              </Box>
            ))}
          </Box>
        ))}
      </Box>
    );
  };

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
        <Box  sx={
      {
      width:"20%",

      }}>
           <Typography   variant="p" style={{ color: theme.palette.primary.main }} >Tasks </Typography>

        </Box>

        <Robots grid={grid} />
      </Box>
       



      </Box>
       
      </ThemeProvider>



    </div>
  );
}

export default App;
