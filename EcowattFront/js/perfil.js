const API_URL = "http://localhost:8080";

let usuarioId = null;


// ==============================
// INICIALIZAÇÃO
// ==============================

document.addEventListener("DOMContentLoaded", () => {

    carregarPerfil();


    document
        .getElementById("formPerfil")
        .addEventListener(
            "submit",
            atualizarPerfil
        );


    document
        .getElementById("btnLogout")
        .addEventListener(
            "click",
            logout
        );

});

// ==============================
// LOGOUT
// ==============================

function logout(){


    localStorage.removeItem("token");


    localStorage.removeItem("usuario");


    window.location.href =
        "login.html";


}



// ==============================
// CARREGAR PERFIL
// ==============================

async function carregarPerfil() {


    const token = localStorage.getItem("token");


    const usuarioSalvo =
        JSON.parse(
            localStorage.getItem("usuario")
        );



    if(!token || !usuarioSalvo){


        alert(
            "Usuário não autenticado."
        );


        window.location.href =
            "login.html";


        return;

    }



    usuarioId =
        usuarioSalvo.id;



    try{


        const resposta =
            await fetch(

                `${API_URL}/usuario/buscar/${usuarioId}`,

                {

                    method:"GET",

                    headers:{

                        "Authorization": token,

                        "Content-Type":
                        "application/json"

                    }

                }

            );



        if(!resposta.ok){

            throw new Error(
                "Erro ao buscar usuário"
            );

        }



        const usuario =
            await resposta.json();



        preencherCampos(usuario);



    }
    catch(error){


        console.error(error);


        alert(
            "Erro ao carregar perfil."
        );


    }


}




// ==============================
// PREENCHER CAMPOS
// ==============================

function preencherCampos(usuario){



    document.getElementById("nome")
        .value =
        usuario.nome;



    document.getElementById("email")
        .value =
        usuario.email;



    document.getElementById("dataRegistro")
        .innerText =
        formatarData(
            usuario.dataRegistro
        );



    document.getElementById("avatarInicial")
        .innerText =
        usuario.nome
            .charAt(0)
            .toUpperCase();


}




// ==============================
// ATUALIZAR PERFIL
// ==============================

async function atualizarPerfil(event){


    event.preventDefault();



    const nome =
        document.getElementById("nome")
            .value;



    const senha =
        document.getElementById("senha")
            .value;



    const confirmarSenha =
        document.getElementById("confirmarSenha")
            .value;



    if(
        senha &&
        senha !== confirmarSenha
    ){

        alert(
            "As senhas não conferem."
        );

        return;

    }



    const dados = {


        nome: nome,


        senha:
            senha || null


    };



    const token =
        localStorage.getItem("token");



    try{


        const resposta =
            await fetch(

                `${API_URL}/usuario/alterar/${usuarioId}`,

                {

                    method:"PUT",


                    headers:{

                        "Authorization": token,

                        "Content-Type":
                        "application/json"

                    },


                    body:
                    JSON.stringify(dados)

                }

            );



        if(!resposta.ok){


            throw new Error(
                "Erro ao atualizar usuário"
            );


        }



        mostrarToast();



        document.getElementById("senha")
            .value = "";



        document.getElementById("confirmarSenha")
            .value = "";



    }
    catch(error){


        console.error(error);


        alert(
            "Erro ao atualizar perfil."
        );


    }



}




// ==============================
// TOAST
// ==============================

function mostrarToast(){


    const toast =
        document.getElementById(
            "toastSucesso"
        );



    toast.classList.add(
        "show"
    );



    setTimeout(()=>{


        toast.classList.remove(
            "show"
        );


    },3000);


}





// ==============================
// FORMATAR DATA
// ==============================

function formatarData(data){


    if(!data){

        return "--";

    }



    const dataFormatada =
        new Date(data);



    return dataFormatada
        .toLocaleDateString(
            "pt-BR"
        );


}