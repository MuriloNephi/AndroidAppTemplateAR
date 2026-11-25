# Resumo das entregas — Hide and Seek

Jogo: esconde-esconde com camaleão em Realidade Aumentada (base: branch `fase-2`).

Legenda: [x] pronto · [ ] depende de mim (print ou vídeo)

## Atividade 1 — Localização + Pontuação

- [x] Link da branch (pronto): https://github.com/MuriloNephi/AndroidAppTemplateAR/tree/feature-location-score
- [ ] Print (endereço + pontuação) → **EU preciso tirar** e salvar em [`prints/pontuacao.png`](../prints/pontuacao.png). Mostre a HUD com os pontos de J1/J2 e o endereço
- [x] Explicação da pontuação (pronta): [`ENTREGAS/atividade-1-location-score.md`](https://github.com/MuriloNephi/AndroidAppTemplateAR/blob/feature-location-score/ENTREGAS/atividade-1-location-score.md), seção "Como a pontuação funciona"

## Atividade 2 — Game Over

- [x] Link da branch (pronto): https://github.com/MuriloNephi/AndroidAppTemplateAR/tree/feature-game-over
- [ ] Vídeo curto (derrota + reinício) → **EU preciso gravar** e salvar em [`videos/game-over.mp4`](../videos/game-over.mp4). O roteiro está no documento abaixo
- [x] Descrição da regra (pronta): [`ENTREGAS/atividade-2-game-over.md`](https://github.com/MuriloNephi/AndroidAppTemplateAR/blob/feature-game-over/ENTREGAS/atividade-2-game-over.md)

## Atividade 3 — Slides

- [x] Arquivo .pptx (pronto): [`ENTREGAS/apresentacao-jogo-ar.pptx`](https://github.com/MuriloNephi/AndroidAppTemplateAR/blob/feature-game-over/ENTREGAS/apresentacao-jogo-ar.pptx). Falta trocar `[SEU NOME]` no slide 1 e inserir as imagens dos 4 objetos 3D no slide 3
- [ ] Vídeo de 30 s ou mais → **EU preciso gravar** e salvar em [`videos/demo-30s.mp4`](../videos/demo-30s.mp4), depois inserir no slide 6
- [x] Roteiro de fala (pronto): [`ENTREGAS/roteiro-fala.md`](https://github.com/MuriloNephi/AndroidAppTemplateAR/blob/feature-game-over/ENTREGAS/roteiro-fala.md)

---

### Depois de gravar

Coloque os arquivos nas pastas e envie:

```powershell
git checkout feature-location-score
git add prints/pontuacao.png
git commit -m "adiciona print da pontuação"
git push

git checkout feature-game-over
git merge feature-location-score
git add videos/game-over.mp4 videos/demo-30s.mp4
git commit -m "adiciona vídeos do jogo"
git push
```

> Vídeos grandes: o GitHub recusa arquivos acima de 100 MB. Se passar disso, compacte o vídeo ou publique no YouTube/Drive e coloque o link aqui.
