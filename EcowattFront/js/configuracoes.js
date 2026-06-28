const usuario =
JSON.parse(
    localStorage.getItem("usuario")
);

const token =
localStorage.getItem("token");


// ==========================
// VALIDA LOGIN
// ==========================
if(!usuario || !usuario.id || !token){

    alert("Usuário não logado");

    window.location.href =
    "login.html";

    throw new Error(
        "Usuário/token inexistente"
    );
}


const usuarioId =
usuario.id;


const API =
"http://localhost:8080/configuracao";


let configuracaoAtual = null;


// ==========================
// AUTH
// ==========================
function authHeader(){

    return {

        "Content-Type":
        "application/json",

        "Authorization":
        token

    };

}



// ==========================
// ELEMENTOS
// ==========================
const form =
document.getElementById(
    "formConfiguracao"
);


const configId =
document.getElementById(
    "configId"
);


const meta =
document.getElementById(
    "meta"
);


const valorTarifa =
document.getElementById(
    "valorTarifa"
);


const unidadeMedida =
document.getElementById(
    "unidadeMedida"
);



// ==========================
// CARREGAR CONFIG DO USUARIO
// ==========================
async function carregarConfiguracao(){

    try{

        const token =
        localStorage.getItem("token");


        const response =
        await fetch(
            `${API}/usuario/${usuarioId}`,
            {
                headers:{
                    "Authorization": token
                }
            }
        );


        if(!response.ok){

            console.log(
                "Usuário ainda não possui configuração"
            );

            configuracaoAtual = null;

            return;
        }


        configuracaoAtual =
        await response.json();


        preencherTela();


    }catch(error){

        console.error(
            "Erro ao carregar configuração:",
            error
        );

    }

}



// ==========================
// PREENCHE FORM
// ==========================
function preencherTela(){


    configId.value =
    configuracaoAtual.id;



    meta.value =
    configuracaoAtual.meta;



    valorTarifa.value =
    configuracaoAtual.valorTarifa;



    unidadeMedida.value =
    configuracaoAtual.unidadeMedida;



    atualizarCards();


}




// ==========================
// CARDS LATERAIS
// ==========================
function atualizarCards(){


    document.getElementById(
        "metaAtual"
    ).innerText =

    configuracaoAtual.meta;



    document.getElementById(
        "tarifaAtual"
    ).innerText =

    "R$ " +
    Number(
        configuracaoAtual.valorTarifa
    ).toFixed(2);



    document.getElementById(
        "unidadeAtual"
    ).innerText =

    configuracaoAtual.unidadeMedida;


}




// ==========================
// SALVAR / ATUALIZAR
// ==========================
form.addEventListener(
"submit",
async function(e){


    e.preventDefault();



    const payload = {


        valorTarifa:
        Number(
            valorTarifa.value
        ),



        meta:
        Number(
            meta.value
        ),



        unidadeMedida:
        unidadeMedida.value


    };



    try{


        let response;



        // JÁ EXISTE CONFIG
        if(configuracaoAtual){


            response =
            await fetch(
                `${API}/alterar/${configuracaoAtual.id}`,
                {

                    method:"PUT",

                    headers:
                    authHeader(),

                    body:
                    JSON.stringify(payload)

                }
            );


        }

        // PRIMEIRA CONFIG
        else{


            payload.usuarioId =
            usuarioId;



            response =
            await fetch(
                `${API}/add`,
                {

                    method:"POST",

                    headers:
                    authHeader(),

                    body:
                    JSON.stringify(payload)

                }
            );


        }



        if(!response.ok){

            throw new Error(
                `Erro ${response.status}`
            );

        }



        configuracaoAtual =
        await response.json();



        preencherTela();



        alert(
            "Configuração salva com sucesso"
        );



    }catch(error){


        console.error(
            error
        );


        alert(
            "Erro ao salvar configuração"
        );

    }


});




// ==========================
// INICIO
// ==========================
carregarConfiguracao();