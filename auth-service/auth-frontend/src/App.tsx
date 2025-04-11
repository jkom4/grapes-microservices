import React from "react";
import {BrowserRouter as Router, Route, Routes} from "react-router-dom";
import Home from './pages/Home';
import LearnMore from './pages/LearnMore';

const App: React.FC = () => {
  return (
      <Router>
        <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/learn-more" element={<LearnMore />} />
        </Routes>
      </Router>
  );
};

export default App;
