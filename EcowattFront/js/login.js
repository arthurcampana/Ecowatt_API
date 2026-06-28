const form =
document.getElementById("LoginForm");

const mensagem =
document.getElementById("mensagem");

const btnLogin =
document.getElementById("btnLogin");

function mostrarMensagem(texto, tipo){

    mensagem.innerHTML = `
        <div class="${tipo}">
            ${texto}
        </div>
    `;
}

form.addEventListener(
    "submit",
    async function(event){

        event.preventDefault();

        mensagem.innerHTML = "";

        const email =
        document.getElementById("email")
        .value
        .trim();

        const senha =
        document.getElementById("senha")
        .value;

        if(!email || !senha){

            mostrarMensagem(
                "Preencha todos os campos.",
                "erro"
            );

            return;
        }

        const dados = {

            email,
            senha
        };

        try{

            btnLogin.disabled = true;

            btnLogin.innerText =
            "Entrando...";

            const response =
            await fetch(
                "http://localhost:8080/usuario/login",
                {
                    method: "POST",

                    headers: {
                        "Content-Type":
                        "application/json"
                    },

                    body: JSON.stringify(dados)
                }
            );

            if(response.ok){

                const resposta =
await response.json();

const usuario = {

    id: resposta.id,
    nome: resposta.nome,
    email: resposta.email
};

localStorage.setItem(
    "usuario",
    JSON.stringify(usuario)
);

localStorage.setItem(
    "token",
    resposta.token
);

                mostrarMensagem(
                    "Login realizado com sucesso!",
                    "sucesso"
                );

                setTimeout(() => {

                    window.location.href =
                    "index.html";

                }, 1000);

                return;
            }

            let erroTexto =
            "Email ou senha inválidos.";

            try{

                const erro =
                await response.text();

                if(erro){

                    erroTexto = erro;
                }

            }catch(e){

                console.error(e);
            }

            mostrarMensagem(
                erroTexto,
                "erro"
            );

        }catch(error){

            console.error(error);

            mostrarMensagem(
                "Não foi possível conectar ao servidor.",
                "erro"
            );

        }finally{

            btnLogin.disabled = false;

            btnLogin.innerText =
            "Fazer Login";
        }

    }
);