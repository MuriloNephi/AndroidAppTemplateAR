# Atividade 2 — Game Over

## Link da branch

https://github.com/MuriloNephi/AndroidAppTemplateAR/tree/feature-game-over

Commits desta atividade (em cima da `feature-location-score`):

| Commit | Descrição |
|--------|-----------|
| [8b3ef05](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/8b3ef0533309c8ab848748c016806bd1a2beeee4) | adiciona vidas, cronômetro e condição de derrota |
| [b8d465e](https://github.com/MuriloNephi/AndroidAppTemplateAR/commit/b8d465e40a2944d1e7f66c5db65dcc7dc4f6e289) | exibe tela de game over e botão jogar novamente |

## Regra de derrota

> Defini a derrota por tempo com vidas. O jogador começa com **3 vidas** e tem **3 minutos** para encontrar um ponto turístico **novo**. Se o cronômetro chegar a zero antes disso, ele perde 1 vida e o cronômetro recomeça. Encontrar um ponto novo também recomeça o cronômetro. Quando as vidas chegam a **0**, a partida termina. **Para dar Game Over basta ficar 3 rodadas seguidas (9 minutos) sem encontrar nenhum ponto novo.**
>
> No Game Over o jogo para o cronômetro, a pontuação, o GPS, a sessão de realidade aumentada, a câmera e o modelo 3D. Em seguida mostra uma tela com o motivo ("O tempo acabou antes de você encontrar um novo ponto turístico e suas vidas chegaram a zero."), a pontuação final e os locais encontrados. O botão **Jogar novamente** zera vidas, pontuação, locais encontrados e cronômetro, volta ao modelo inicial e reabre a sessão AR.
>
> Durante o jogo, a HUD mostra a pontuação, os locais (ex.: 2/7), as vidas (♥♥♡) e o tempo restante.

Detalhes técnicos:
- Os valores ficam em constantes de `Partida.kt`: `VIDAS_INICIAIS = 3`, `TEMPO_POR_RODADA_MS = 3 min` e `PONTOS_POR_DESCOBERTA = 10`.
- O cronômetro pausa quando a tela do jogo não está visível (outra aba ou app em segundo plano).
- Ao encontrar todos os pontos, o cronômetro para (objetivo concluído).

## Roteiro do vídeo curto — `videos/game-over.mp4`

Arquivo: [`videos/game-over.mp4`](../videos/game-over.mp4)

> A derrota leva 9 minutos de jogo. Sugestão: grave tudo, sem sair do app, e depois corte ou acelere o trecho de espera no editor. Não troque de aba nem bloqueie a tela, porque isso pausa o cronômetro.

1. **(0–5 s)** Abra o app. Mostre a HUD com `Pontuação: 0`, `Vidas: ♥♥♥` e `Tempo: 3:00` contando, e o endereço atual.
2. **(acelerado)** Deixe o tempo correr até zerar. Mostre a mensagem "O tempo acabou! Você perdeu uma vida. Restam 2." e a HUD com `♥♥♡` e o tempo de volta em 3:00.
3. **(acelerado)** Repita até a HUD mostrar `♥♡♡`.
4. **(tempo real, ~10 s)** Mostre o último minuto até o tempo zerar. Aparece a tela **GAME OVER** com o motivo e a pontuação final. Mostre que a câmera/AR parou.
5. **(~5 s)** Toque em **Jogar novamente**. Mostre a HUD de volta a `Pontuação: 0`, `Vidas: ♥♥♥`, `Tempo: 3:00` e a câmera/AR reaberta.

Dica: se der para encontrar um ponto turístico antes (ex.: perto da Catedral), mostre a pontuação subindo para 10. Assim a tela de Game Over exibe uma pontuação final diferente de zero.
