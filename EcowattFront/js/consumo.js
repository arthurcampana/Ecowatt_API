const usuario =
JSON.parse(
    localStorage.getItem("usuario")
);

const token =
localStorage.getItem("token");


// ==========================
// VALIDA LOGIN
// ==========================

if(
    !usuario ||
    !usuario.id ||
    !token
){

    alert("Usuário não logado");

    window.location.href =
    "login.html";
}



const usuarioId =
usuario.id;


const API =
"http://localhost:8080/consumo";



const form =
document.getElementById("formConsumo");


const lista =
document.getElementById("listaConsumos");


const formCard =
document.getElementById("cardFormulario");


const btnAbrir =
document.getElementById("btnAbrirFormulario");


const btnFechar =
document.getElementById("btnFecharFormulario");


// ==========================
// VARIÁVEL GLOBAL
// ==========================

let todosConsumos = [];




// ==========================
// ABRIR FORM
// ==========================

btnAbrir.addEventListener(
"click",
()=>{

    abrirFormulario();

});




// ==========================
// FECHAR FORM
// ==========================

btnFechar.addEventListener(
"click",
()=>{

    fecharFormulario();

});





function abrirFormulario(){


    formCard.classList.add(
        "active"
    );


    window.scrollTo({

        top:0,

        behavior:"smooth"

    });

}





function fecharFormulario(){


    formCard.classList.remove(
        "active"
    );


    form.reset();


    document.getElementById(
        "consumoId"
    ).value="";


    document.getElementById(
        "tituloFormulario"
    ).innerText =
    "Novo Consumo";


}






// ==========================
// FILTRO
// ==========================


document
.getElementById("btnFiltrar")
.addEventListener(
"click",
filtrarConsumos
);





function filtrarConsumos(){


    const mes =
    document.getElementById(
        "filtroMes"
    ).value;



    const ano =
    document.getElementById(
        "filtroAno"
    ).value;



    let filtrados =
    todosConsumos;



    if(mes !== ""){


        filtrados =
        filtrados.filter(item=>{


            const data =
            new Date(
                item.dataRegistro
            );


            return data.getMonth()
            ===
            Number(mes);


        });


    }






    if(ano){


        filtrados =
        filtrados.filter(item=>{


            const data =
            new Date(
                item.dataRegistro
            );


            return data.getFullYear()
            ===
            Number(ano);


        });


    }




    renderizarConsumos(
        filtrados
    );


}








// ==========================
// LISTAR
// ==========================

async function carregarConsumos(){


    try{


        const response =
        await fetch(
            `${API}/usuario/${usuarioId}`,
            {

                headers:{

                    Authorization:token

                }

            }
        );



        if(!response.ok){

            throw new Error(
                "Erro ao buscar consumos"
            );

        }





        const dados =
        await response.json();



        todosConsumos =
        dados;



        renderizarConsumos(
            todosConsumos
        );




    }catch(error){


        console.error(error);



        lista.innerHTML =
        `

        <div class="consumo-card">

            Erro ao carregar consumos

        </div>

        `;

    }


}









// ==========================
// RENDER
// ==========================

function renderizarConsumos(dados){


    lista.innerHTML="";



    if(!dados || dados.length===0){


        lista.innerHTML =
        `

        <div class="consumo-card">

            Nenhum consumo encontrado.

        </div>

        `;


        return;

    }





    const ordenado =
    [...dados].reverse();





    ordenado.forEach(item=>{


        lista.innerHTML +=
        `


        <div class="consumo-card">



            <div class="consumo-info">


                <h5>

                    ${Number(
                        item.consumoKwh
                    ).toFixed(2)}
                    kWh

                </h5>




                <p>

                    ${formatarData(
                        item.dataRegistro
                    )}

                </p>




                <div class="badge-consumo">


                    Registro #${item.id}


                </div>



            </div>





            <div class="consumo-actions">



                <button

                    class="btn-action btn-edit"

                    onclick="editarConsumo(${item.id})">


                    Editar


                </button>





                <button

                    class="btn-action btn-delete"

                    onclick="deletarConsumo(${item.id})">


                    Excluir


                </button>



            </div>



        </div>


        `;



    });



}







function formatarData(data){


    if(!data){

        return "Sem data";

    }


    return new Date(data)
    .toLocaleString("pt-BR");


}









// ==========================
// SALVAR
// ==========================

form.addEventListener(
"submit",
async(e)=>{


    e.preventDefault();



    try{



        const id =
        document.getElementById(
            "consumoId"
        ).value;





        const payload = {


            usuarioId,


            consumoKwh:
            Number(
                document.getElementById(
                    "consumoKwh"
                ).value
            ),




            dataRegistro:

            document.getElementById(
                "dataRegistro"
            ).value || null


        };





        let url =
        `${API}/add`;



        let method =
        "POST";




        if(id){


            url =
            `${API}/${id}`;


            method =
            "PUT";


        }





        const response =
        await fetch(
            url,
            {

                method,


                headers:{

                    "Content-Type":
                    "application/json",

                    Authorization:token

                },



                body:
                JSON.stringify(payload)

            }

        );






        if(!response.ok){


            throw new Error(
                await response.text()
            );


        }





        fecharFormulario();



        carregarConsumos();





    }catch(error){


        console.error(error);


        alert(
            "Erro ao salvar consumo"
        );


    }


});









// ==========================
// EDITAR
// ==========================

async function editarConsumo(id){


    try{


        const response =
        await fetch(
            `${API}/${id}`,
            {

                headers:{

                    Authorization:token

                }

            }

        );





        const item =
        await response.json();




        abrirFormulario();



        document.getElementById(
            "tituloFormulario"
        ).innerText =
        "Editar Consumo";





        document.getElementById(
            "consumoId"
        ).value =
        item.id;





        document.getElementById(
            "consumoKwh"
        ).value =
        item.consumoKwh;





        document.getElementById(
            "dataRegistro"
        ).value =
        item.dataRegistro
        ?
        item.dataRegistro.substring(0,16)
        :
        "";




    }catch(error){

        console.error(error);

    }

}









// ==========================
// DELETE
// ==========================

async function deletarConsumo(id){



    if(!confirm(
        "Deseja excluir esse consumo?"
    )){

        return;

    }





    await fetch(
        `${API}/${id}`,
        {

            method:"DELETE",

            headers:{

                Authorization:token

            }

        }

    );



    carregarConsumos();


}







// INICIALIZA

carregarConsumos();