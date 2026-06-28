function authHeader(){

    return {
        "Content-Type":"application/json",
        "Authorization": token
    };
}

const token =
localStorage.getItem("token");

if(!token){

    alert("Usuário não logado");

    window.location.href =
    "login.html";

    throw new Error(
        "Token não encontrado"
    );
}

const usuario =
JSON.parse(
    localStorage.getItem("usuario")
);

if(!usuario || !usuario.id){

    alert(
        "Dados do usuário não encontrados"
    );

    window.location.href =
    "login.html";

    throw new Error(
        "Usuário não encontrado"
    );
}

const usuarioId =
usuario.id;

const lista =
document.getElementById("listaEquipamentos");

const selectEquip =
document.getElementById("equipamento");

const horasInput =
document.getElementById("horasPorDia");

const consumoPreview =
document.getElementById("consumoEsperado");

const formCard =
document.getElementById("cardFormulario");

const btnAbrir =
document.getElementById("btnAbrirFormulario");

const btnFechar =
document.getElementById("btnFecharFormulario");

const form =
document.getElementById("formEquipUser");

let equipamentosCache = [];

// ==========================
// ABRIR FORM
// ==========================
btnAbrir.addEventListener("click", async () => {

    form.reset();

    formCard.classList.add("active");

    document.getElementById(
        "tituloFormulario"
    ).innerText =
    "Novo equipamento";

    document.getElementById(
        "equipUserId"
    ).value = "";

    consumoPreview.innerText = "0.00";

    await carregarEquipamentosBase();

    formCard.scrollIntoView({
        behavior:"smooth",
        block:"start"
    });

});

// ==========================
// FECHAR FORM
// ==========================
btnFechar.addEventListener("click", () => {

    formCard.classList.remove("active");

    form.reset();

    consumoPreview.innerText = "0.00";

});

// ==========================
// BASE
// ==========================
async function carregarEquipamentosBase(){

    try{

        const response =
        await fetch(
            "http://localhost:8080/equipamentos/listar",
            {
                headers: authHeader()
            }
        );

        if(!response.ok){

            throw new Error(
                `Erro ${response.status}`
            );
        }

        equipamentosCache =
        await response.json();

        selectEquip.innerHTML =
        `<option value="">
            Selecione um equipamento
        </option>`;

        equipamentosCache.forEach(eq => {

            selectEquip.innerHTML += `
            <option value="${eq.id}">
                ${eq.nome}
            </option>
            `;

        });

    }catch(error){

        console.error(
            "Erro ao carregar equipamentos:",
            error
        );

    }

}

// ==========================
// CONSUMO
// ==========================
function atualizarConsumo(){

    const equipamentoId =
    Number(selectEquip.value);

    const horas =
    Number(horasInput.value);

    if(!equipamentoId || !horas){

        consumoPreview.innerText = "0.00";

        return;
    }

    const equipamento =
    equipamentosCache.find(
        e => Number(e.id) === equipamentoId
    );

    if(!equipamento){

        consumoPreview.innerText = "0.00";

        return;
    }

    const total =
    Number(equipamento.consumoPorHora)
    * horas;

    consumoPreview.innerText =
    total.toFixed(2);

}

selectEquip.addEventListener(
    "change",
    atualizarConsumo
);

horasInput.addEventListener(
    "input",
    atualizarConsumo
);

// ==========================
// LISTAR
// ==========================
async function carregarEquipamentosUsuario(){

    try{

        const response =
        await fetch(
            `http://localhost:8080/equipamentousuario/listar/${usuarioId}`,
            {
                headers: authHeader()
            }
        );

        if(!response.ok){

            throw new Error(
                `Erro ${response.status}`
            );
        }

        const dados =
        await response.json();

        renderizarEquipamentos(dados);

    }catch(error){

        console.error(
            "Erro ao carregar equipamentos do usuário:",
            error
        );

    }

}

// ==========================
// RENDER
// ==========================
function renderizarEquipamentos(dados){

    lista.innerHTML = "";

    dados.reverse().forEach(item => {

        lista.insertAdjacentHTML(
            "afterbegin",
            `

            <div class="equip-card">

                <div class="equip-info">

                    <h5>
                        ${item.nomeIdentificacao}
                    </h5>

                    <p>
    ${item.nomeEquipamento}
</p>

                    <div class="badge-consumo">

                        ${Number(item.consumoEsperado).toFixed(2)}
                        kWh/dia

                    </div>

                </div>

                <div class="equip-actions">

                    <button
                    class="btn-action btn-edit"
                    onclick="editarEquipamento(${item.id})">

                    Editar

                    </button>

                    <button
                    class="btn-action btn-delete"
                    onclick="deletarEquipamento(${item.id})">

                    Excluir

                    </button>

                </div>

            </div>

            `
        );

    });

}

// ==========================
// EDITAR
// ==========================
async function editarEquipamento(id){

    try{

        await carregarEquipamentosBase();

        const response =
        await fetch(
            `http://localhost:8080/equipamentousuario/${id}`,
            {
                headers: authHeader()
            }
        );

        if(!response.ok){

            throw new Error(
                `Erro ${response.status}`
            );
        }

        const item =
        await response.json();

        formCard.classList.add("active");

        document.getElementById(
            "tituloFormulario"
        ).innerText =
        "Editar equipamento";

        document.getElementById(
            "equipUserId"
        ).value =
        item.id;

        document.getElementById(
            "nomeIdentificacao"
        ).value =
        item.nomeIdentificacao;

        document.getElementById(
            "horasPorDia"
        ).value =
        item.horasPorDia;

        console.log(item)
        selectEquip.value = String(item.equipamentoId);


        atualizarConsumo();

        formCard.scrollIntoView({
            behavior:"smooth",
            block:"start"
        });

    }catch(error){

        console.error(
            "Erro ao editar equipamento:",
            error
        );

    }

}
// ==========================
// DELETE
// ==========================
async function deletarEquipamento(id){

    try{

        const response =
        await fetch(
            `http://localhost:8080/equipamentousuario/${id}`,
            {
                method:"DELETE",
                headers: authHeader()
            }
        );

        if(!response.ok){

            throw new Error(
                `Erro ${response.status}`
            );
        }

        carregarEquipamentosUsuario();

    }catch(error){

        console.error(
            "Erro ao excluir equipamento:",
            error
        );

    }

}

// ==========================
// SALVAR
// ==========================
form.addEventListener("submit", async (e) => {

    e.preventDefault();

    try{

        const id =
        document.getElementById(
            "equipUserId"
        ).value;

       const payload = {

    nomeIdentificacao:
    document.getElementById(
        "nomeIdentificacao"
    ).value,


    horasPorDia:
    Number(
        document.getElementById(
            "horasPorDia"
        ).value
    ),


    consumoEsperado:
    Number(consumoPreview.innerText),


    usuarioId:
    usuarioId,


    equipamentoId:
    Number(selectEquip.value)

};

        let url =
        "http://localhost:8080/equipamentousuario/add";

        let method =
        "POST";

        if(id){

            url =
            `http://localhost:8080/equipamentousuario/${id}`;

            method =
            "PUT";

        }
        
        const response =
        await fetch(url,{

            method:method,

            headers: authHeader(),

            body:JSON.stringify(payload)

        });

        if(!response.ok){

            throw new Error(
                `Erro ${response.status}`
            );
        }

        formCard.classList.remove(
            "active"
        );

        carregarEquipamentosUsuario();

    }catch(error){

        console.error(
            "Erro ao salvar equipamento:",
            error
        );

    }

});

// ==========================
// NOVO EQUIPAMENTO
// ==========================
document.getElementById(
    "btnSalvarEquipamento"
).addEventListener(
    "click",
    async () => {

        try{

            const payload = {

                nome:
                document.getElementById(
                    "novoNome"
                ).value,

                modelo:
                document.getElementById(
                    "novoModelo"
                ).value,

                consumoPorHora:
                Number(
                    document.getElementById(
                        "novoConsumo"
                    ).value
                )

            };

            const response =
            await fetch(
                "http://localhost:8080/equipamentos/add",
                {
                    method:"POST",

                    headers: authHeader(),

                    body:JSON.stringify(payload)
                }
            );

            if(!response.ok){

                throw new Error(
                    `Erro ${response.status}`
                );
            }

            const novoEquip =
            await response.json();

            await carregarEquipamentosBase();

            selectEquip.value =
            String(novoEquip.id);

            atualizarConsumo();

            bootstrap.Modal.getInstance(
                document.getElementById(
                    "modalEquipamento"
                )
            ).hide();

        }catch(error){

            console.error(
                "Erro ao cadastrar equipamento:",
                error
            );

        }

    }
);

// ==========================
carregarEquipamentosBase();
carregarEquipamentosUsuario();