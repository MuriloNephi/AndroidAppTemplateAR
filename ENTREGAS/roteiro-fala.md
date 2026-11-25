# Roteiro de fala — Apresentação Bioverse-AR

Slides: [`apresentacao-jogo-ar.pptx`](apresentacao-jogo-ar.pptx). Tempo total: cerca de 6 a 7 minutos, mais o vídeo.

---

## Slide 1 — Capa (~30 s)

> Olá, eu sou [SEU NOME] e vou apresentar o **Bioverse-AR**, um jogo de Realidade Aumentada para Android. A ideia é transformar um passeio pelos pontos turísticos de Ponta Grossa num jogo de exploração. Você caminha pela cidade e, em cada local, aparece um objeto 3D na tela do celular, como se ele estivesse ali no ambiente. Vou mostrar como o jogo funciona, os objetos 3D, a pontuação, a condição de derrota e, no final, um vídeo do jogo rodando.

## Slide 2 — Objetivo e como jogar (~50 s)

> O objetivo é descobrir os sete pontos turísticos cadastrados antes que as vidas acabem. Jogar é simples. Primeiro, o jogador ativa a localização: no topo da tela aparecem o endereço atual, a pontuação, as vidas e o tempo. Depois, ele vai até um ponto turístico. Quando entra no raio de detecção do local, o objeto 3D daquele ponto é carregado. Aí ele aponta a câmera para o chão ou para uma mesa e toca em "Posicionar" para fixar o objeto no ambiente. E tem o desafio do relógio: são três minutos para achar um ponto novo, senão ele perde uma vida.

## Slide 3 — Objetos 3D (~40 s)

> Estes são os três objetos 3D do jogo, todos no formato glb, na pasta de assets do app. O **neutrófilo**, uma célula de defesa do corpo humano, que combina com a identidade "Bio" do jogo. O **plano**, um modelo simples de superfície. E o **sofá**. Cada ponto turístico tem um objeto fixo associado. O neutrófilo aparece na Catedral, no Parque Ambiental e em Vila Velha. O plano, no Museu Campos Gerais e na Mansão Vila Hilda. O sofá, na Estação Saudade e no Buraco do Padre.

## Slide 4 — Sistema de pontuação (~45 s)

> A pontuação é direta: cada ponto turístico **novo** vale **dez pontos**. Como são sete pontos, o máximo por partida é setenta. Para não dar para "farmar" pontos, guardo os locais já encontrados num conjunto, um Set: cada local pontua só uma vez por partida. Se o jogador voltar a um lugar já visitado, o objeto aparece de novo, mas a pontuação não muda. A pontuação, o número de locais encontrados e o endereço atual ficam sempre visíveis no topo da tela, atualizados em tempo real.

## Slide 5 — Game Over (~50 s)

> A derrota é por tempo, com vidas. O jogador começa com três vidas e tem três minutos para encontrar um ponto novo. Se o cronômetro zera, ele perde uma vida e o relógio recomeça. Encontrar um ponto novo também reinicia o tempo. Quando as vidas chegam a zero, é Game Over. Nesse momento a partida é encerrada de verdade: param o cronômetro, a pontuação, o GPS, a sessão de realidade aumentada, a câmera e o objeto 3D. Aparece uma tela com o motivo da derrota e a pontuação final, e o botão "Jogar novamente" zera tudo e começa uma partida nova.

## Slide 6 — O jogo em execução (~10 s de fala + vídeo)

> Agora vou mostrar o jogo funcionando no celular. Reparem na pontuação no topo da tela e, no final, na tela de Game Over.

*(Reproduzir `videos/demo-30s.mp4`.)*

## Slide 7 — Os 7 pontos turísticos (extra, ~30 s)

> Esta é a lista dos pontos turísticos. Ficam todos centralizados num único arquivo do código, o `PontosTuristicos.kt`, com nome, latitude, longitude, raio de detecção e objeto 3D. Os pontos do centro têm raio menor, de 50 a 80 metros, porque estão próximos uns dos outros. Os parques naturais, como Buraco do Padre e Vila Velha, têm raio maior. As coordenadas vieram do OpenStreetMap.

## Slide 8 — Tecnologias e desafios (extra, ~40 s)

> O jogo foi feito em Kotlin para Android, usando ViewModel e LiveData para manter o estado da partida. A realidade aumentada usa ARCore com a biblioteca SceneView. Se o aparelho não tiver ARCore, o jogo usa a câmera comum, pelo CameraX. A localização vem do GPS e o endereço do Geocoder. Os maiores desafios foram lidar com a imprecisão do GPS, encerrar AR, câmera e GPS com segurança no Game Over, e manter a partida ao trocar de aba ou girar a tela. Obrigado!

---

## Roteiro do vídeo de 30 s ou mais — `videos/demo-30s.mp4`

Objetivo: mostrar o jogo rodando, **a pontuação subindo** e **o Game Over**. Grave a tela do celular (gravador nativo do Android) e depois edite.

| Tempo | O que mostrar |
|-------|---------------|
| 0–5 s | App abrindo. HUD no topo com `Pontuação: 0`, `Vidas: ♥♥♥`, `Tempo: 3:00` e o endereço atual. |
| 5–12 s | Câmera apontada para o chão; aparece "Superfície encontrada"; toque em **Posicionar** e o objeto 3D aparece no ambiente. |
| 12–20 s | Chegando a um ponto turístico (ex.: perto da Catedral Sant'Ana): a mensagem "+10 pontos! Você encontrou: …" aparece e a HUD muda para `Pontuação: 10 • Locais: 1/7`. |
| 20–25 s | (Trecho acelerado no editor) o tempo zera e aparece "O tempo acabou! Você perdeu uma vida". A HUD mostra `♥♥♡`. |
| 25–32 s | Última vida acabando: tela **GAME OVER** com o motivo e a pontuação final. |
| 32–35 s | Toque em **Jogar novamente**: HUD de volta a `Pontuação: 0`, `♥♥♥`, `3:00`. |

Dicas:
- Não troque de aba nem bloqueie a tela durante a espera: isso **pausa** o cronômetro.
- Para mostrar a pontuação subindo é preciso estar fisicamente num dos pontos. O mais prático é o centro (Catedral, Museu Campos Gerais, Parque Ambiental e Estação Saudade ficam a poucos quarteirões).
- Você pode reaproveitar trechos de `videos/game-over.mp4` (Atividade 2).
