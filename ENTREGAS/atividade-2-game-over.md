# Atividade 2 — Game Over

Jogo: **Hide and Seek**, um esconde-esconde com o camaleão em Realidade Aumentada.

## Link da branch

https://github.com/MuriloNephi/AndroidAppTemplateAR/tree/feature-game-over

Commits desta atividade (em cima da `feature-location-score`):

| Commit | Descrição |
|--------|-----------|
| [d20c98d](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/d20c98db785800aca684f71f1613bf67b3449525) | adiciona chances de quem procura e condição de derrota |
| [4506aa5](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/4506aa5108aba3bc65bddb98abf1ed5a142ceee5) | exibe tela de game over e botão jogar novamente |

> Os commits anteriores desta branch com uma regra de cronômetro foram desfeitos com `git revert`, porque não correspondiam ao jogo.

## Regra de derrota

> No Hide and Seek, um jogador esconde e pinta o camaleão e o outro procura tocando na tela. Quem procura tem **3 chances** por rodada (os corações na HUD). Cada toque **fora** do camaleão tira uma chance. Se ele achar o camaleão, ganha 10 pontos, os papéis se invertem e as chances voltam a 3 na rodada seguinte. Se as chances chegarem a **0**, a partida termina. **Para dar Game Over basta, na vez de procurar, tocar 3 vezes em lugares onde o camaleão não está.**
>
> No Game Over o jogo encerra a partida: para o GPS, a sessão de realidade aumentada, a câmera, o personagem e os toques, e não soma mais pontos. Aparece uma tela com o motivo (ex.: "O Jogador 2 tocou fora do personagem 3 vezes e ficou sem chances."), o placar final dos dois jogadores e o vencedor. O botão **Jogar novamente** zera pontos, chances, papéis e locais visitados, volta ao camaleão original e reabre a sessão AR.

O valor fica na constante `CHANCES_POR_RODADA = 3`, em `Partida.kt`.

## Roteiro do vídeo curto — `videos/game-over.mp4`

Arquivo: [`videos/game-over.mp4`](../videos/game-over.mp4). Duração sugerida: 30 a 45 s.

1. **(0–8 s) Esconder:** com o Jogador 1, aponte para o chão, toque em **Posicionar**, escolha uma cor na paleta (ex.: verde) e toque em **Pronto! Passar o celular**.
2. **(8–12 s) Passar o celular:** mostre a tela "Passe o celular para o Jogador 2" e toque em **Começar a procurar**. A HUD mostra `Chances: ♥♥♥`.
3. **(12–18 s) Errar uma vez:** toque longe do camaleão. Aparece "Errou! … Restam 2 chance(s)." e a HUD fica com `♥♥♡`.
4. **(18–22 s) Errar de novo:** a HUD fica com `♥♡♡`.
5. **(22–30 s) Game Over:** toque fora pela terceira vez. Aparece a tela **GAME OVER** com o motivo, o placar e o vencedor. A câmera/AR para.
6. **(30–35 s) Reinício:** toque em **Jogar novamente**. A HUD volta a `J1: 0 pts • J2: 0 pts`, "Jogador 1 esconde", e a câmera/AR é reaberta.

Dica: para o placar final não ficar zerado, jogue antes uma rodada em que o Jogador 2 acha o camaleão (+10).
