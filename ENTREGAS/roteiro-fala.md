# Roteiro de fala — Apresentação Hide and Seek

Slides: [`apresentacao-jogo-ar.pptx`](apresentacao-jogo-ar.pptx). Tempo total: cerca de 6 a 7 minutos, mais o vídeo.

---

## Slide 1 — Capa (~30 s)

> Olá, eu sou [SEU NOME] e vou apresentar o **Hide and Seek**, um jogo de esconde-esconde em Realidade Aumentada para Android. A ideia é simples: um jogador esconde um camaleão virtual na imagem da câmera, pinta ele para se disfarçar e passa o celular para o amigo do lado, que tem que encontrá-lo. Vou mostrar como se joga, os objetos 3D, a pontuação, a condição de derrota e, no final, um vídeo do jogo rodando.

## Slide 2 — Objetivo e como jogar (~50 s)

> São dois jogadores e um celular só. Na sua vez de esconder, você aponta a câmera para algum lugar e arrasta o camaleão até o esconderijo, por exemplo atrás de um vaso ou sobre o sofá. Dá para deixá-lo menor com os botões de tamanho. Depois você disfarça: escolhe na paleta uma cor parecida com o lugar, verde na planta, marrom na mesa, cinza no chão. Aí toca em "Pronto": a imagem da câmera congela, como uma foto, e a tela fica coberta para o outro jogador não ver onde você escondeu. Você passa o celular, e quem procura toca na foto onde acha que o camaleão está. Se acertar, pontua e passa a ser quem esconde.

## Slide 3 — Objetos 3D (~40 s)

> Estes são os objetos 3D do jogo, todos em formato glb. O principal é o **camaleão**, o personagem que se esconde. O animal combina com a mecânica, porque o camaleão muda de cor para se camuflar, e é isso que o jogador faz com a paleta. Na aba "change" dá para trocar o personagem por outros modelos do projeto: o **neutrófilo**, o **sofá** e um **plano** simples. Assim dá para variar e esconder objetos diferentes.

## Slide 4 — Sistema de pontuação (~45 s)

> Quem encontra o camaleão ganha **dez pontos** e passa a esconder na rodada seguinte. Também usei a localização do celular: cadastrei sete pontos turísticos de Ponta Grossa e, quando os jogadores chegam a um deles pela primeira vez na partida, quem está com o celular ganha **cinco pontos de bônus**. Guardo os locais já visitados num conjunto, um Set, para que cada um só dê bônus uma vez. No topo da tela ficam sempre os pontos dos dois jogadores, de quem é a vez, as chances e o endereço atual.

## Slide 5 — Game Over (~50 s)

> Quem procura não pode sair tocando em tudo: tem **três chances** por rodada, que aparecem como corações na tela. Cada toque fora do camaleão tira uma chance. Se achar o camaleão, as chances voltam a três na próxima rodada. Mas se as chances chegarem a zero, é Game Over. A partida é encerrada de verdade: param o GPS, a câmera, a cena 3D, o personagem e a pontuação. Aparece uma tela com o motivo da derrota, o placar dos dois jogadores e o vencedor, e o botão "Jogar novamente" zera tudo para uma nova partida.

## Slide 6 — O jogo em execução (~10 s de fala + vídeo)

> Agora vou mostrar o jogo funcionando. Reparem na pontuação subindo quando o camaleão é encontrado e, no final, na tela de Game Over.

*(Reproduzir `videos/demo-30s.mp4`.)*

## Slide 7 — Bônus de localização (extra, ~30 s)

> Estes são os sete pontos turísticos. Ficam numa lista única no código, com nome, latitude, longitude e raio de detecção. Os do centro têm raio menor, de 50 a 80 metros, porque estão perto uns dos outros. Os parques naturais, como Buraco do Padre e Vila Velha, têm raio maior. As coordenadas vieram do OpenStreetMap. A ideia é incentivar a jogar em lugares diferentes da cidade.

## Slide 8 — Tecnologias e desafios (extra, ~40 s)

> O jogo foi feito em Kotlin para Android, com ViewModel e LiveData para manter o estado da partida. A imagem da câmera vem do CameraX, e o camaleão 3D é desenhado por cima com a biblioteca SceneView. Por isso o jogo funciona em qualquer celular com câmera, sem precisar de ARCore. Os maiores desafios foram:
> - saber se o toque acertou o modelo 3D ou o vazio;
> - pintar o camaleão alterando o material do modelo;
> - congelar a imagem e esconder a tela na troca de jogador;
> - encerrar câmera e GPS com segurança no Game Over.
>
> Obrigado!

---

## Roteiro do vídeo de 30 s ou mais — `videos/demo-30s.mp4`

Objetivo: mostrar o jogo rodando, **a pontuação subindo** e **o Game Over**. Grave a tela do celular (gravador nativo do Android) com outra pessoa jogando junto.

| Tempo | O que mostrar |
|-------|---------------|
| 0–8 s | App aberto, HUD com `J1: 0 pts • J2: 0 pts`, "Jogador 1 esconde" e o endereço. Jogador 1 aponta a câmera para um lugar e arrasta o camaleão até o esconderijo. |
| 8–13 s | Jogador 1 diminui o camaleão com **−**, toca numa cor da paleta (ex.: verde) e o camaleão muda de cor. Toca em **Pronto! Passar o celular**: a imagem congela. |
| 13–16 s | Tela "Passe o celular para o Jogador 2". Jogador 2 toca em **Começar a procurar**. HUD: `Chances: ♥♥♥`. |
| 16–22 s | Jogador 2 erra uma vez (`♥♥♡`) e depois toca no camaleão: som, "+10 pontos" e HUD `J2: 10 pts`, agora "Jogador 2 esconde". |
| 22–30 s | Jogador 2 esconde, passa o celular, e o Jogador 1 erra 3 vezes. |
| 30–35 s | Tela **GAME OVER** com o motivo, o placar e o vencedor (Jogador 2). Toque em **Jogar novamente**. |

Dica: você pode reaproveitar trechos de `videos/game-over.mp4` (Atividade 2).
