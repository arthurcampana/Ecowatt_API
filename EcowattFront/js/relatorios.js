const usuario = JSON.parse(localStorage.getItem("usuario"));
const token = localStorage.getItem("token");
let graficoEquipamentos = null;
let graficoCustoEquipamentos = null;
let graficoParticipacao = null;

if (!usuario || !usuario.id || !token) {
    alert("Usuário não logado");
    window.location.href = "login.html";
}

const usuarioId = usuario.id;

const API_CONSUMO = "http://localhost:8080/consumo";
const API_CONFIG = "http://localhost:8080/configuracao";
if (!token) {
    location.href = "login.html";
}

function authHeader() {
    return {
        "Content-Type": "application/json",
        "Authorization": token
    };
}

function diasDoMes(data){

    const d = new Date(data);

    return new Date(
        d.getFullYear(),
        d.getMonth() + 1,
        0
    ).getDate();

}



let consumos = [];
let consumosFiltrados = [];
let configuracao = null;

let grafico = null;
let tipoGrafico = "bar";

const painel = document.getElementById("painelRelatorio");

document
    .getElementById("btnGerar")
    .addEventListener("click", gerarRelatorio);

document
    .getElementById("btnTrocarGrafico")
    .addEventListener("click", trocarGrafico);

document
    .getElementById("btnExportar")
    .addEventListener("click", exportarPDF);

window.onload = async () => {

    await carregarConfiguracao();

    await carregarConsumos();

};

async function carregarConfiguracao() {

    try {

        const response =
            await fetch(
                `${API_CONFIG}/usuario/${usuarioId}`,
                {
                    headers: authHeader()
                }
            );

        if (!response.ok) {

            configuracao = {
                meta: 0,
                valorTarifa: 0
            };

            return;

        }

        configuracao =
            await response.json();

    }

    catch {

        configuracao = {
            meta: 0,
            valorTarifa: 0
        };

    }

}




async function carregarConsumos() {

    try {

        const response =
            await fetch(
                `http://localhost:8080/consumo/usuario/${usuarioId}`,
                {
                    headers: authHeader()
                }
            );

        if (!response.ok)
            throw new Error();

        todosConsumos = await response.json();

        console.log("Consumos:", todosConsumos);

    }

    catch (e) {

        console.error(e);

        alert("Erro ao carregar consumos.");

    }

}

function gerarRelatorio() {
console.log("Gerar relatório")
    painel.style.display = "block";

    aplicarFiltros();

    atualizarCards();

    atualizarTabela();

    atualizarGrafico();

    carregarEquipamentos();

}

function aplicarFiltros(){
    console.log(todosConsumos);
    const inicio = document.getElementById("dataInicial").value;
    const fim = document.getElementById("dataFinal").value;

    consumosFiltrados = todosConsumos.filter(item => {

        const data = new Date(item.dataRegistro);

        if(inicio){

            const dataInicio = new Date(inicio);

            if(data < dataInicio)
                return false;
        }

        if(fim){

            const dataFim = new Date(fim);
            dataFim.setHours(23,59,59,999);

            if(data > dataFim)
                return false;
        }

        return true;

    });

}
function atualizarCards() {
    if (!configuracao) {
    alert("Nenhuma configuração cadastrada.");
    return;
}

    const total =
        consumosFiltrados.reduce(
            (soma, item) => soma + Number(item.consumoKwh),
            0
        );

    const media =
        consumosFiltrados.length == 0
            ? 0
            : total / consumosFiltrados.length;

const maior =
    consumosFiltrados.length == 0
        ? 0
        : Math.max(
            ...consumosFiltrados.map(
                c => Number(c.consumoKwh)
            )
        );

const menor =
    consumosFiltrados.length == 0
        ? 0
        : Math.min(
            ...consumosFiltrados.map(
                c => Number(c.consumoKwh)
            )
        );

    const tarifa =
        Number(configuracao.valorTarifa);

    const custo =
        total * tarifa;

    document.getElementById(
        "consumoTotal"
    ).innerHTML =
        total.toFixed(2) + " kWh";

    document.getElementById(
        "consumoMedio"
    ).innerHTML =
        media.toFixed(2) + " kWh";

    document.getElementById(
        "maiorConsumo"
    ).innerHTML =
        maior.toFixed(2) + " kWh";

    document.getElementById(
        "menorConsumo"
    ).innerHTML =
        menor.toFixed(2) + " kWh";

    document.getElementById(
        "custoTotal"
    ).innerHTML =
        "R$ " + custo.toFixed(2);

    document.getElementById(
        "metaAtual"
    ).innerHTML =
        configuracao.meta + " kWh";

    const status =
        document.getElementById(
            "statusMeta"
        );

    if(total <= configuracao.meta){

        status.innerHTML =
            "Dentro da Meta";

        status.className =
            "status-ok";

    }else{

        status.innerHTML =
            "Meta Ultrapassada";

        status.className =
            "status-alerta";

    }

}

function atualizarTabela() {

    console.log("Entrou em atualizarTabela");

    console.log("Quantidade:", consumosFiltrados.length);
    const tbody = document.getElementById("tabelaConsumos");

    tbody.replaceChildren();

    consumosFiltrados
        .sort((a, b) => new Date(a.dataRegistro) - new Date(b.dataRegistro))
        .forEach(item => {

            const tr = document.createElement("tr");

            tr.innerHTML = `
                <td>${formatarData(item.dataRegistro)}</td>
                
                <td>${Number(item.consumoKwh).toFixed(2)} kWh</td>
            `;

            tbody.appendChild(tr);

        });

}
function formatarData(data){

    const d =
        new Date(data);

    return d.toLocaleDateString(
        "pt-BR"
    );

}
function atualizarGrafico() {

    if (grafico) {
        grafico.destroy();
    }

    let labels = [];
    let valores = [];

    const mapa = {};

    let agruparPorMes = false;

    const dataInicial = document.getElementById("dataInicial").value;
    const dataFinal = document.getElementById("dataFinal").value;

    if (dataInicial && dataFinal) {

        const inicio = new Date(dataInicial);
        const fim = new Date(dataFinal);

        const dias =
            (fim - inicio) / (1000 * 60 * 60 * 24);

        agruparPorMes = dias > 31;
    }

    consumosFiltrados.forEach(c => {

        let chave;

        if (agruparPorMes) {

            chave = new Date(c.dataRegistro)
                .toLocaleDateString(
                    "pt-BR",
                    {
                        month: "short",
                        year: "numeric"
                    }
                );

        } else {

            chave = formatarData(c.dataRegistro);

        }

        mapa[chave] =
            (mapa[chave] || 0) +
            Number(c.consumoKwh);

    });

    labels = Object.keys(mapa);
    valores = Object.values(mapa);

    grafico = new Chart(

        document
            .getElementById("graficoConsumo"),

        {

            type: tipoGrafico,

            data: {

                labels: labels,

                datasets: [{

                    label: "Consumo (kWh)",

                    data: valores,

                    borderWidth: 2,

                    fill: false

                }]

            },

            options: {

                responsive: true,

                maintainAspectRatio: false

            }

        }

    );

}

function trocarGrafico(){

    tipoGrafico =
        tipoGrafico == "bar"
        ? "line"
        : "bar";

    atualizarGrafico();

}

function obterSemana(data){

    const d =
        new Date(data);

    const primeiro =
        new Date(
            d.getFullYear(),
            0,
            1
        );

    const dias =
        Math.floor(
            (d-primeiro)/
            86400000
        );

    return "Semana " +
        Math.ceil(
            (dias+
            primeiro.getDay()+1)
            /7
        );

}


async function exportarPDF() {

    if (consumosFiltrados.length == 0) {

        alert("Gere um relatório primeiro.");
        return;

    }

    const area = document.getElementById("painelRelatorio");

    const canvas = await html2canvas(area, {
        scale: 2,
        useCORS: true
    });

    const imgData = canvas.toDataURL("image/png");

    const { jsPDF } = window.jspdf;

    const pdf = new jsPDF("p", "mm", "a4");

    const pageWidth = 210;
    const pageHeight = 297;

    const margin = 10;

    const imgWidth = pageWidth - (margin * 2);

    const imgHeight =
        canvas.height * imgWidth / canvas.width;

    let heightLeft = imgHeight;

    let position = margin;

    pdf.setFontSize(18);
    pdf.text("Relatório EcoWatt", 60, 15);

    pdf.setFontSize(11);
    pdf.text(
        "Data: " + new Date().toLocaleDateString("pt-BR"),
        margin,
        25
    );

    position = 35;

    pdf.addImage(
        imgData,
        "PNG",
        margin,
        position,
        imgWidth,
        imgHeight
    );

    heightLeft -= (pageHeight - position);

    while (heightLeft > 0) {

        position = heightLeft - imgHeight;

        pdf.addPage();

        pdf.addImage(
            imgData,
            "PNG",
            margin,
            position,
            imgWidth,
            imgHeight
        );

        heightLeft -= pageHeight;

    }

    pdf.save("Relatorio_EcoWatt.pdf");

}
async function carregarEquipamentos(){

    try{

        const response = await fetch(
            "http://localhost:8080/equipamentousuario/listar/" + usuarioId,
            {
                headers: authHeader()
            }
        );

        if(!response.ok){
            throw new Error("Erro ao buscar equipamentos");
        }

        const dados = await response.json();

        gerarGraficoEquipamentos(dados);

        gerarGraficoCustoEquipamentos(dados);



    }catch(e){

        console.error(e);

    }

}

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

    dados.forEach(item=>{

        labels.push(item.nomeIdentificacao);
        consumos.push(Number(item.consumoEsperado));

    });

    if(graficoEquipamentos){
        graficoEquipamentos.destroy();
    }

    const ctx =
        document
            .getElementById("graficoEquipamentos")
            .getContext("2d");

    graficoEquipamentos = new Chart(ctx,{

        type:"doughnut",

        data:{

            labels:labels,

            datasets:[{

                data:consumos,

                backgroundColor:cores,

                borderColor:"#fff",

                borderWidth:3

            }]

        },

        options:{

            responsive:true,

            maintainAspectRatio:false,

            plugins:{

                legend:{
                    display:false
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
function gerarLegendaEquipamentos(
    labels,
    consumos,
    cores
){

    const legenda =
        document.getElementById(
            "legendaEquipamentos"
        );

    if(!legenda){
        return;
    }

    legenda.innerHTML = "";

    labels.forEach((label,index)=>{

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
                    ${consumos[index].toFixed(2)} kWh/dia
                </div>

            </div>

        `;

    });

}

function gerarGraficoCustoEquipamentos(equipamentos){

    if(!equipamentos.length)
        return;

    const consumoTotal =
        consumosFiltrados.reduce(
            (soma,c)=>soma + Number(c.consumoKwh),
            0
        );

    const tarifa =
        Number(configuracao.valorTarifa);

    const somaEsperado =
        equipamentos.reduce(
            (soma,e)=>soma + Number(e.consumoEsperado),
            0
        );

    const labels = [];
    const custos = [];

    equipamentos.forEach(e=>{

        const percentual =
            Number(e.consumoEsperado) / somaEsperado;

        const consumoEstimado =
            consumoTotal * percentual;

        const custo =
            consumoEstimado * tarifa;

        labels.push(e.nomeIdentificacao);

        custos.push(custo);

    });

    if(graficoCustoEquipamentos){

        graficoCustoEquipamentos.destroy();

    }

    graficoCustoEquipamentos =
        new Chart(

            document
                .getElementById("graficoCustoEquipamentos"),

            {

                type:"bar",

                data:{

                    labels:labels,

                    datasets:[{

                        label:"Custo estimado (R$)",

                        data:custos,

                        borderWidth:2,

                        borderRadius:8

                    }]

                },

                options:{

                    responsive:true,

                    maintainAspectRatio:false,

                    plugins:{

                        tooltip:{

                            callbacks:{

                                label:function(context){

                                    return "R$ "
                                        + context.raw.toFixed(2);

                                }

                            }

                        }

                    },

                    scales:{

                        y:{

                            beginAtZero:true,

                            ticks:{

                                callback:function(value){

                                    return "R$ " + value;

                                }

                            }

                        }

                    }

                }

            }

        );

}
