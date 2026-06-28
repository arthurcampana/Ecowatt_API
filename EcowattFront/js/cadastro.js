const API =
    "http://localhost:8080/usuario/add";

const form =
    document.getElementById(
        "cadastroForm"
    );

const mensagem =
    document.getElementById(
        "mensagem"
    );

function mostrarMensagem(
    texto,
    tipo
){

    mensagem.innerHTML = `

        <div class="${tipo}">
            ${texto}
        </div>

    `;
}

function limparMensagem(){

    mensagem.innerHTML = "";
}

form.addEventListener(
    "submit",
    async (event) => {

        event.preventDefault();

        limparMensagem();

        const nome =
            document
                .getElementById("nome")
                .value
                .trim();

        const email =
            document
                .getElementById("email")
                .value
                .trim();

        const senha =
            document
                .getElementById("senha")
                .value;

        const confirmarSenha =
            document
                .getElementById(
                    "confirmarSenha"
                )
                .value;

        if(
            !nome ||
            !email ||
            !senha
        ){

            mostrarMensagem(
                "Preencha todos os campos.",
                "erro"
            );

            return;
        }

        if(senha.length < 6){

            mostrarMensagem(
                "A senha deve ter pelo menos 6 caracteres.",
                "erro"
            );

            return;
        }

        if(
            senha !==
            confirmarSenha
        ){

            mostrarMensagem(
                "As senhas não coincidem.",
                "erro"
            );

            return;
        }

        const payload = {

            nome,
            email,
            senha
        };

        try{

            const response =
                await fetch(
                    API,
                    {
                        method:"POST",

                        headers:{
                            "Content-Type":
                            "application/json"
                        },

                        body:
                        JSON.stringify(
                            payload
                        )
                    }
                );

            if(response.ok){

                mostrarMensagem(
                    "Usuário cadastrado com sucesso!",
                    "sucesso"
                );

                form.reset();

                setTimeout(() => {

                    window.location.href =
                    "login.html";

                },1500);

                return;
            }

            const erro =
                await response.text();

            mostrarMensagem(
                erro ||
                "Erro ao cadastrar usuário.",
                "erro"
            );

        }catch(error){

            console.error(error);

            mostrarMensagem(
                "Não foi possível conectar ao servidor.",
                "erro"
            );
        }
    }
);