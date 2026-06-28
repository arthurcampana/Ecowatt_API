const usuario =
JSON.parse(
    localStorage.getItem("usuario")
);

const token =
localStorage.getItem("token");

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

// ==========================
// BUSCAR DADOS
// ==========================
async function carregarConsumos(){

    try{

        const response =
        await fetch(
            `http://localhost:8080/consumo/usuario/${usuarioId}`,
            {
                headers:{
                    Authorization: token
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

        processarDados(dados);

    } catch(error){

        console.error(
            "Erro ao buscar consumos:",
            error
        );
    }
}

// ==========================
// PROCESSAR DADOS
// ==========================
function processarDados(consumos){

    const anoAtual =
    new Date().getFullYear();

    const consumoMes =
    new Array(12).fill(0);

    consumos.forEach(c => {

        if(!c.dataRegistro){
            return;
        }

        const [ano, mes] =
        c.dataRegistro
            .split("T")[0]
            .split("-");

        const anoNum =
        parseInt(ano);

        const mesIndex =
        parseInt(mes) - 1;

        if(anoNum === anoAtual){

            consumoMes[mesIndex] +=
            c.consumoKwh;
        }
    });

    atualizarCards(consumoMes);

    gerarGrafico(consumoMes);

    carregarEquipamentos();
}

// ==========================
// CARDS
// ==========================
function atualizarCards(consumoMes){

    const mesAtual =
    new Date().getMonth();

    const totalMes =
    consumoMes[mesAtual];

    const totalAno =
    consumoMes.reduce(
        (acc, valor) => acc + valor,
        0
    );

    const mesesComConsumo =
    consumoMes.filter(v => v > 0).length || 1;

    const media =
    totalAno / mesesComConsumo;

    document.getElementById(
        "consumoMes"
    ).innerText =
    totalMes.toFixed(2) + " kWh";

    document.getElementById(
        "consumoAno"
    ).innerText =
    totalAno.toFixed(2) + " kWh";

    document.getElementById(
        "mediaMensal"
    ).innerText =
    media.toFixed(2) + " kWh";
}

// ==========================
// GRAFICO CONSUMO
// ==========================
function gerarGrafico(consumoMes){

    const labels = [
        "Jan","Fev","Mar","Abr",
        "Mai","Jun","Jul","Ago",
        "Set","Out","Nov","Dez"
    ];

    const ctx =
    document
        .getElementById("graficoConsumo")
        .getContext("2d");

    new Chart(ctx,{

        type:"bar",

        data:{

            labels:labels,

            datasets:[{

                label:
                "Consumo por mês (kWh)",

                data:consumoMes,

                borderWidth:2,

                borderRadius:8
            }]
        },

        options:{

            responsive:true,

            maintainAspectRatio:false
        }
    });
}

// ==========================
// EQUIPAMENTOS
// ==========================
async function carregarEquipamentos(){

    try{

        const response =
        await fetch(
            `http://localhost:8080/equipamentousuario/listar/${usuarioId}`,
            {
                headers:{
                    Authorization: token
                }
            }
        );

        if(!response.ok){

            throw new Error(
                "Erro ao buscar equipamentos"
            );
        }

        const dados =
        await response.json();

        gerarGraficoEquipamentos(dados);

    } catch(err){

        console.error(
            "Erro equipamentos:",
            err
        );
    }
}

// ==========================
// GRAFICO EQUIPAMENTOS
// ==========================
function gerarGraficoEquipamentos(dados){

    const labels = [];

    const consumos = [];

    const cores = [
        "#198754",
        "#0d6efd",
        "#ffc107",
        "#dc3545",
        "#6f42c1",
        "#20c997",
        "#fd7e14",
        "#6610f2",
        "#0dcaf0",
        "#6c757d"
    ];

    dados.forEach(item => {

        labels.push(
            item.nomeIdentificacao
        );

        consumos.push(
            item.consumoEsperado
        );
    });

    const ctx =
    document
        .getElementById("graficoEquipamentos")
        .getContext("2d");

    new Chart(ctx,{

        type:"doughnut",

        data:{

            labels:labels,

            datasets:[{

                data:consumos,

                backgroundColor:cores,

                borderColor:"#ffffff",

                borderWidth:3
            }]
        },

        options:{

            responsive:true,

            maintainAspectRatio:false,

            plugins:{

                legend:{
                    display:false
                },

                tooltip:{

                    callbacks:{

                        label:function(context){

                            return (
                                context.label +
                                ": " +
                                context.raw.toFixed(2) +
                                " kWh/dia"
                            );
                        }
                    }
                }
            }
        }
    });

    gerarLegendaEquipamentos(
        labels,
        consumos,
        cores
    );
}

// ==========================
// LEGENDA
// ==========================
function gerarLegendaEquipamentos(
    labels,
    consumos,
    cores
){

    const legenda =
        document.getElementById(
            "legendaEquipamentos"
        );

    legenda.innerHTML = "";

    labels.forEach((label, index) => {

        legenda.innerHTML += `

        <div class="legenda-item">

            <div class="legenda-left">

                <div
                    class="legenda-cor"
                    style="background:${cores[index]}">
                </div>

                <div>

                    <div class="legenda-titulo">

                        ${label}

                    </div>

                    <div class="legenda-sub">

                        Equipamento registrado

                    </div>

                </div>

            </div>

            <div class="legenda-valor">

                ${consumos[index].toFixed(2)}
                kWh/dia

            </div>

        </div>

        `;

    });

}


// ==========================
carregarConsumos();