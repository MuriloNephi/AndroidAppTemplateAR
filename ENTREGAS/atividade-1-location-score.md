# Atividade 1 — Localização + Pontuação

## Link da branch

https://github.com/MuriloNephi/AndroidAppTemplateAR/tree/feature-location-score

## Commits enviados

Saída de `git log --oneline main..HEAD`:

| Commit | Descrição |
|--------|-----------|
| [fb35736](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/fb3573637823169f2d6a60113c04177e2532597a) | exibe a pontuação e o endereço atual na tela |
| [7647872](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/7647872f98c6450e836c18c15bfe79c7b6081caa) | adiciona a lógica de pontuação da partida |
| [7c9a9aa](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/7c9a9aaad8bec8bcc2750d2e7b05dc799dd2ec9d) | centraliza os pontos turísticos em uma lista de configuração |
| [85a475a](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/85a475a01e192df0d5a4b5e9ec495e1c1a31046e) | Merge da branch feature-location (upstream) |
| [2496fb2](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/2496fb260c612e57917ce1523df7d22b44e201cb) | exibe a localização do usuário em tempo real *(veio da feature-location)* |
| [ed41614](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/ed416147360d2cf6a823c8fc512513747280e88c) | Delete src.zip *(veio da feature-location)* |
| [87eef8d](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/87eef8dbd1966c829c47537061932279734b778d) | altera o sceneview *(veio da feature-location)* |

Também há um commit que adiciona este documento de entrega e as pastas `prints/` e `videos/`.

## Pontos turísticos cadastrados

A lista fica em `app/src/main/java/com/ifpr/androidapptemplatear/jogo/PontosTuristicos.kt`. As coordenadas foram obtidas no OpenStreetMap.

| Ponto | Latitude | Longitude | Raio | Modelo 3D | Situação |
|-------|----------|-----------|------|-----------|----------|
| Catedral Sant'Ana | -25.0982041 | -50.1589514 | 60 m | neutrofilo.glb | alterado |
| Museu Campos Gerais | -25.0970006 | -50.1585938 | 50 m | plane.glb | novo |
| Estação Saudade | -25.0933828 | -50.1532606 | 60 m | sofa.glb | novo |
| Parque Ambiental Gov. Manoel Ribas | -25.0957335 | -50.1549645 | 80 m | neutrofilo.glb | novo |
| Mansão Vila Hilda | -25.0917531 | -50.1620343 | 60 m | plane.glb | novo |
| Buraco do Padre | -25.1719374 | -49.9688266 | 150 m | sofa.glb | novo |
| Parque Estadual de Vila Velha | -25.2329936 | -50.0116978 | 300 m | neutrofilo.glb | alterado |

A Igreja dos Polacos foi removida.

## Print (endereço atual + pontuação)

![Endereço atual e pontuação](../prints/pontuacao.png)

Arquivo: `prints/pontuacao.png`

## Como a pontuação funciona

> Implementei a pontuação do jogo somando pontos sempre que o jogador encontra um ponto turístico novo. Cadastrei sete pontos reais de Ponta Grossa numa lista única. Cada ponto tem nome, latitude, longitude, raio de detecção e o modelo 3D que aparece ao chegar. O app acompanha o GPS e, quando entro no raio de um ponto, carrega o modelo dele em realidade aumentada. Se o ponto ainda não tinha sido encontrado na partida, ganho 10 pontos (constante `PONTOS_POR_DESCOBERTA`). Guardo os pontos já encontrados num conjunto (`Set`), então voltar ao mesmo lugar mostra o modelo de novo, mas não pontua outra vez. No topo da tela aparecem sempre a pontuação, quantos locais já encontrei (por exemplo, 2/7) e o endereço atual, obtido por geocoding reverso, tudo atualizado em tempo real.
