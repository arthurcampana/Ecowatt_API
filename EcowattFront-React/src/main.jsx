import React from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import App from "./App.jsx";
import { AuthProvider } from "./context/AuthContext.jsx";
import { ThemeProvider } from "./context/ThemeContext.jsx";
import { UIProvider } from "./context/UIContext.jsx";
import "./styles/global.css";

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <BrowserRouter>
      <ThemeProvider>
        <UIProvider>
          <AuthProvider>
            <App />
          </AuthProvider>
        </UIProvider>
      </ThemeProvider>
    </BrowserRouter>
  </React.StrictMode>
);
