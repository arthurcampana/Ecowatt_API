import { createContext, useContext, useEffect, useState } from "react";

const ThemeContext = createContext(null);

const THEME_KEY = "ecowatt-theme";

function lerTemaSalvo() {
  try {
    const salvo = localStorage.getItem(THEME_KEY);
    if (salvo === "dark" || salvo === "light") {
      return salvo;
    }
  } catch {
    /* ignora */
  }
  return "light";
}

// Controla o tema (claro/escuro) do app. O valor fica em localStorage e
// e aplicado via atributo data-theme no <html>, que o CSS usa para trocar
// as variaveis de cor.
export function ThemeProvider({ children }) {
  const [tema, setTema] = useState(lerTemaSalvo);

  useEffect(() => {
    document.documentElement.setAttribute("data-theme", tema);
    try {
      localStorage.setItem(THEME_KEY, tema);
    } catch {
      /* ignora */
    }
  }, [tema]);

  function alternarTema() {
    setTema((atual) => (atual === "dark" ? "light" : "dark"));
  }

  return (
    <ThemeContext.Provider value={{ tema, alternarTema }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const ctx = useContext(ThemeContext);
  if (!ctx) {
    throw new Error("useTheme deve ser usado dentro de <ThemeProvider>");
  }
  return ctx;
}
