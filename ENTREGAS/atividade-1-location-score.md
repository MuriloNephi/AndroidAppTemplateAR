# Atividade 1 — Localização + Pontuação

Jogo: **Hide and Seek**, um esconde-esconde com o camaleão em Realidade Aumentada para dois jogadores no mesmo celular.

## Link da branch

https://github.com/MuriloNephi/AndroidAppTemplateAR/tree/feature-location-score

## Commits enviados

Saída de `git log --oneline main..HEAD`:

| Commit | Descrição |
|--------|-----------|
| [32e2416](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/32e241625fb04415e010ecbbb8d92e3bcfd801f4) | implementa esconder, pintar e procurar o camaleão na tela |
| [36614c3](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/36614c390fc4fd1498521e64c28f1230c20d3966) | troca a lógica para esconde-esconde entre dois jogadores |
| [d389034](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/d38903403887c8d249f8f201d7cd6584a877d8af) | Merge da branch fase-2 (camaleão, som e nome do jogo) |
| [346180c](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/346180c6e136c8c3b1e55944a5335a9a406b41ef) | adiciona documento de entrega da atividade 1 e pastas de mídia |
| [fb35736](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/fb3573637823169f2d6a60113c04177e2532597a) | exibe a pontuação e o endereço atual na tela |
| [7647872](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/7647872f98c6450e836c18c15bfe79c7b6081caa) | adiciona a lógica de pontuação da partida |
| [7c9a9aa](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/7c9a9aaad8bec8bcc2750d2e7b05dc799dd2ec9d) | centraliza os pontos turísticos em uma lista de configuração |
| [85a475a](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/85a475a01e192df0d5a4b5e9ec495e1c1a31046e) | Merge da branch feature-location (upstream) |
| [2496fb2](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/2496fb260c612e57917ce1523df7d22b44e201cb) | exibe a localização do usuário em tempo real *(veio da feature-location)* |
| [ed41614](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/ed416147360d2cf6a823c8fc512513747280e88c) | Delete src.zip *(veio da feature-location)* |
| [87eef8d](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/87eef8dbd1966c829c47537061932279734b778d) | altera o sceneview *(veio da feature-location)* |
| [e778f0b](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/e778f0bfe8429d70083242f7819b9bd21ce4b7d5) | Fase 2: novos modelos 3D, toque com pontuacao, sons e creditos *(veio da fase-2)* |

## Pontos turísticos cadastrados

A lista fica em `app/src/main/java/com/ifpr/androidapptemplatear/jogo/PontosTuristicos.kt`. As coordenadas foram obtidas no OpenStreetMap. Cada ponto dá um bônus na primeira visita da partida.

| Ponto | Latitude | Longitude | Raio | Situação |
|-------|----------|-----------|------|----------|
| Catedral Sant'Ana | -25.0982041 | -50.1589514 | 60 m | alterado |
| Museu Campos Gerais | -25.0970006 | -50.1585938 | 50 m | novo |
| Estação Saudade | -25.0933828 | -50.1532606 | 60 m | novo |
| Parque Ambiental Gov. Manoel Ribas | -25.0957335 | -50.1549645 | 80 m | novo |
| Mansão Vila Hilda | -25.0917531 | -50.1620343 | 60 m | novo |
| Buraco do Padre | -25.1719374 | -49.9688266 | 150 m | novo |
| Parque Estadual de Vila Velha | -25.2329936 | -50.0116978 | 300 m | alterado |

A Igreja dos Polacos foi removida.

## Print (endereço atual + pontuação)

![Endereço atual e pontuação](../prints/pontuacao.png)

Arquivo: `prints/pontuacao.png`

## Como a pontuação funciona

> O Hide and Seek é um esconde-esconde em realidade aumentada para dois jogadores no mesmo celular. Quem esconde posiciona o camaleão em algum lugar do ambiente, pinta ele com uma cor da paleta para disfarçá-lo e passa o celular. Quem procura toca na tela onde acha que o camaleão está. Se tocar nele, encontrou o modelo e ganha 10 pontos (constante `PONTOS_POR_ACHAR`), toca o som do camaleão e os papéis se invertem: quem achou passa a esconder. Também usei a localização: há sete pontos turísticos de Ponta Grossa numa lista única, e o primeiro jogador a chegar a cada um ganha 5 pontos de bônus (`BONUS_LOCAL`). Cada local só dá bônus uma vez por partida, porque guardo os locais visitados num `Set`. No topo da tela aparecem sempre os pontos dos dois jogadores, de quem é a vez, os locais visitados e o endereço atual, obtido por geocoding reverso e atualizado em tempo real.
