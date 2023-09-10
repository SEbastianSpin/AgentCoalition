import React from 'react';
import SmartToyIcon from '@mui/icons-material/SmartToy';
import Box from '@mui/material/Box';

const Cell = ({ value }) => {
  return (
    <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center' }}>
      {value !== " " && <SmartToyIcon />}
      <Box
        sx={{
          fontSize: '0.75rem',
          opacity: "0.4"
        }}
      >
        {value !== 0 ? value : null}
      </Box>
    </Box>
  );
};

export default Cell;
