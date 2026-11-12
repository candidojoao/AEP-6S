const dia = (deslocamento) => {
  const data = new Date();
  data.setHours(0, 0, 0, 0);
  data.setDate(data.getDate() + deslocamento);
  return data;
};

const quantidade = (valor) => NumberDecimal(String(valor));

const movimentacao = (tipo, valor, diasAtras, observacao) => {
  const registro = { tipo, quantidade: quantidade(valor), data: dia(-diasAtras) };
  if (observacao) {
    registro.observacao = observacao;
  }
  return registro;
};

const SAIDAS = ["CONSUMO", "DESCARTE", "DOACAO"];

const alimento = (nome, residenciaId, categoriaId, local, inicial, unidade, diasParaVencer, movimentacoes) => {
  const saldo = movimentacoes.reduce((total, mov) => {
    const valor = Number(mov.quantidade.toString());
    return SAIDAS.includes(mov.tipo) ? total - valor : total + valor;
  }, inicial);
  return {
    nome,
    residenciaId,
    categoriaId,
    local,
    quantidade: quantidade(Math.round(saldo * 1000) / 1000),
    unidade,
    dataValidade: dia(diasParaVencer),
    movimentacoes,
  };
};

["alimentos", "residencias", "categorias"].forEach((colecao) => db.getCollection(colecao).drop());

db.categorias.createIndex({ nome: 1 }, { name: "nome", unique: true });
db.alimentos.createIndex({ residenciaId: 1, dataValidade: 1 }, { name: "residencia_validade" });
db.alimentos.createIndex({ categoriaId: 1 }, { name: "categoriaId" });

const categoria = {
  laticinios: new ObjectId(),
  carnes: new ObjectId(),
  hortifruti: new ObjectId(),
  graos: new ObjectId(),
  paes: new ObjectId(),
};

db.categorias.insertMany([
  {
    _id: categoria.laticinios,
    nome: "Laticínios",
    diasAlertaValidade: 3,
    dicasConservacao: ["Manter refrigerado entre 1 °C e 5 °C", "Depois de aberto, consumir em até 3 dias"],
  },
  {
    _id: categoria.carnes,
    nome: "Carnes",
    diasAlertaValidade: 2,
    dicasConservacao: ["Congelar se não for usar em até 2 dias", "Descongelar dentro da geladeira"],
  },
  {
    _id: categoria.hortifruti,
    nome: "Hortifrúti",
    diasAlertaValidade: 4,
    dicasConservacao: ["Lavar só na hora de consumir", "Guardar folhas na gaveta da geladeira"],
  },
  {
    _id: categoria.graos,
    nome: "Grãos",
    diasAlertaValidade: 30,
    dicasConservacao: ["Guardar em pote fechado, longe da umidade"],
  },
  {
    _id: categoria.paes,
    nome: "Pães",
    diasAlertaValidade: 2,
    dicasConservacao: ["Congelar fatiado para durar mais"],
  },
]);

const casaSilva = new ObjectId();
const apartamentoJoao = new ObjectId();

db.residencias.insertMany([
  {
    _id: casaSilva,
    nome: "Casa da família Silva",
    responsavel: "Ana Silva",
    endereco: {
      rua: "Rua das Palmeiras",
      numero: "120",
      bairro: "Zona 7",
      cidade: "Maringá",
      estado: "PR",
      cep: "87020-000",
    },
    locais: [
      { nome: "Geladeira", tipo: "REFRIGERADO" },
      { nome: "Freezer", tipo: "CONGELADO" },
      { nome: "Despensa", tipo: "AMBIENTE" },
    ],
  },
  {
    _id: apartamentoJoao,
    nome: "Apartamento do João",
    responsavel: "João Souza",
    endereco: {
      rua: "Avenida Brasil",
      numero: "3500",
      bairro: "Centro",
      cidade: "Maringá",
      estado: "PR",
      cep: "87013-000",
    },
    locais: [
      { nome: "Geladeira", tipo: "REFRIGERADO" },
      { nome: "Armário", tipo: "AMBIENTE" },
    ],
  },
]);

db.alimentos.insertMany([
  alimento("Leite integral", casaSilva, categoria.laticinios, "Geladeira", 4, "L", 2, [
    movimentacao("CONSUMO", 1, 3, "Café da manhã"),
    movimentacao("CONSUMO", 1, 1, "Café da manhã"),
  ]),
  alimento("Iogurte natural", casaSilva, categoria.laticinios, "Geladeira", 6, "un", -1, [
    movimentacao("CONSUMO", 3, 4),
  ]),
  alimento("Queijo minas", casaSilva, categoria.laticinios, "Geladeira", 0.5, "kg", 10, []),
  alimento("Peito de frango", casaSilva, categoria.carnes, "Freezer", 1.5, "kg", 0, [
    movimentacao("CONSUMO", 0.5, 2, "Almoço de domingo"),
  ]),
  alimento("Alface", casaSilva, categoria.hortifruti, "Geladeira", 1, "un", -3, [
    movimentacao("DESCARTE", 1, 1, "Murchou antes de ser usada"),
  ]),
  alimento("Tomate", casaSilva, categoria.hortifruti, "Geladeira", 1, "kg", 3, [
    movimentacao("CONSUMO", 0.4, 2),
    movimentacao("DESCARTE", 0.2, 1, "Dois tomates estragados"),
  ]),
  alimento("Banana", casaSilva, categoria.hortifruti, "Despensa", 12, "un", 1, [
    movimentacao("CONSUMO", 5, 5),
    movimentacao("DOACAO", 3, 3, "Doadas para a vizinha"),
    movimentacao("DESCARTE", 2, 2, "Passaram do ponto"),
    movimentacao("REPOSICAO", 6, 1, "Compra da feira"),
  ]),
  alimento("Arroz", casaSilva, categoria.graos, "Despensa", 5, "kg", 20, [
    movimentacao("CONSUMO", 1, 6),
    movimentacao("DOACAO", 1, 2, "Campanha de arrecadação do bairro"),
  ]),
  alimento("Feijão carioca", casaSilva, categoria.graos, "Despensa", 2, "kg", 90, [
    movimentacao("CONSUMO", 0.5, 4),
  ]),
  alimento("Pão de forma", casaSilva, categoria.paes, "Despensa", 1, "un", -2, [
    movimentacao("DESCARTE", 1, 1, "Mofou"),
  ]),
  alimento("Leite desnatado", apartamentoJoao, categoria.laticinios, "Geladeira", 2, "L", 5, [
    movimentacao("CONSUMO", 0.5, 1),
  ]),
  alimento("Macarrão", apartamentoJoao, categoria.graos, "Armário", 1, "kg", 200, []),
  alimento("Presunto", apartamentoJoao, categoria.carnes, "Geladeira", 0.3, "kg", -1, [
    movimentacao("DESCARTE", 0.1, 1),
  ]),
]);

print("Dados de exemplo carregados no banco " + db.getName() + ":");
print("  categorias:  " + db.categorias.countDocuments());
print("  residencias: " + db.residencias.countDocuments());
print("  alimentos:   " + db.alimentos.countDocuments());
print("");
print("Ids para testar no Swagger:");
print("  Casa da família Silva: " + casaSilva.toString());
print("  Apartamento do João:   " + apartamentoJoao.toString());
