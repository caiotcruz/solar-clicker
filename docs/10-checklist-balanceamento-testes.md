# Solar Clicker — Documento 10: Checklist de Balanceamento & Testes

> Status: rascunho v0.1
> Depende de: todos os documentos anteriores (01–09)
> Este é o último documento do roadmap inicial (doc 0 do projeto). Ele não introduz sistemas novos — consolida tudo que ficou marcado como "pendente de calibragem" e define como validar o jogo antes do lançamento.

## 1. Inventário de Valores Pendentes de Calibragem

Toda fórmula já foi definida nos documentos anteriores — o que falta são os **números**. Esta tabela existe pra você (ou eu, quando formos balancear) não perder nenhum de vista:

| Variável | Onde é definida | Afeta |
|---|---|---|
| `custoBase` por planeta (desbloqueio) | doc 3, seção 0 (hoje só multiplicadores relativos) | Ritmo de progressão principal |
| `taxaCrescimento` (custo de upgrade local/geral) | doc 2, seção 3 | Quão rápido upgrades ficam caros |
| `taxaExportacao` (moeda local → Solar Coins) | doc 2, seção 4 | Dependência de moeda geral vs. locais |
| `baseRotacao(planeta)` / `baseTranslacao(planeta)` | doc 2, seção 7 | Valor de cada "pagamento" de ciclo |
| `requisitoBase` / `crescimentoLocal` (Ascensão Local) | doc 4, seção 2 | Frequência de ascensão local por planeta |
| `fatorBonus(planeta)` (bônus de Ascensão Local) | doc 4, seção 2 | Força do multiplicador permanente por planeta |
| `requisitoBaseEstabilidade` / `crescimentoGeral` | doc 4, seção 3 | Duração de uma run completa até a Ascensão Geral |
| `C` (constante de Pontos Estelares) | doc 4, seção 5 | Quanto cada run "vale" em prestígio |
| Tetos (%) dos nós da Loja de Ascensão Geral | doc 4, seção 6 | Poder máximo da meta-progressão |
| `offlineCapSeconds` (base e por nível de Sono Estelar) | doc 8, seção 4 | Viabilidade da build offline |
| `offlinePenaltyMultiplier` (base e por nível de Vigília Eficiente) | doc 8, seção 4 | Idem |

## 2. Metodologia de Playtest

**Vantagem estrutural (doc 5):** como a camada `domain` não depende de LibGDX, é possível escrever um **simulador headless** — um pequeno programa Java que roda milhares de "horas de jogo" simuladas em segundos, sem abrir janela nenhuma, e imprime métricas. Isso deve ser construído **antes** de tentar calibrar os números manualmente jogando.

**Métricas-alvo a definir (por você, com base na sensação que quer para o jogo):**
- Tempo até desbloquear cada planeta (1º ao 9º), numa run "normal" (sem otimização excessiva) e numa run "otimizada".
- Tempo até a primeira Ascensão Local de cada planeta.
- Duração de uma run completa até a primeira Ascensão Geral.
- Quantas Ascensões Gerais até a Loja de Ascensão Geral estar "razoavelmente" preenchida (não necessariamente completa).
- Ganho percentual de velocidade entre a run N e a run N+1 (isso mede se a meta-progressão está compensando o reset).

**Processo sugerido:**
1. Rodar o simulador com valores-chute (educated guess) baseados nos exemplos ilustrativos já presentes nos documentos.
2. Comparar contra as métricas-alvo.
3. Ajustar as variáveis da seção 1 (uma de cada vez, quando possível, pra entender causa-efeito).
4. Repetir.
5. Só depois disso, validar com playtest humano (a simulação pega desbalanceamento matemático; só um humano pega se o jogo é *chato* apesar de matematicamente equilibrado).

## 3. Checklist de QA Funcional

Antes de considerar qualquer fase "pronta" (não só a final):

- [ ] `BigNumber`: soma, subtração, multiplicação, potência inteira e fracionária, comparação, serialização/deserialização — todos com testes de unidade cobrindo casos extremos (zero, valores próximos do limite de `long` no expoente).
- [ ] Save/Load: salvar, fechar o jogo, reabrir — estado idêntico. Testar migração simulando um save de schema antigo (mesmo que hipotético/manual no início).
- [ ] Escrita atômica de save: matar o processo (kill -9 ou equivalente) no meio de um autosave — o save anterior deve permanecer íntegro.
- [ ] Progresso offline: testar com tempo offline menor que o cap, maior que o cap, e exatamente no limite.
- [ ] `ModifierRegistry`: um upgrade de Netuno afeta corretamente todos os outros planetas; um upgrade de Marte direcionado a um planeta específico afeta só aquele planeta.
- [ ] Ascensão Local: reseta exatamente o que deveria (moeda local, níveis de upgrade) e preserva o resto (desbloqueio do planeta, nível de ascensão).
- [ ] Ascensão Geral: reseta tudo esperado (doc 4, seção 4) e preserva Pontos Estelares + Loja de Ascensão Geral.
- [ ] Caso especial de Plutão: Estabilidade Orbital funciona como gate da Ascensão Geral e não é confundida com moeda comum em nenhum cálculo.
- [ ] i18n: nenhuma chave de texto aparece "crua" na tela (ex: `shop.upgrade.click_power.name` literal) — toda chave usada no código tem entrada correspondente em **ambos** os arquivos de idioma.
- [ ] Câmera: pan (WASD) e zoom respeitam limites (não é possível sair da área navegável nem dar zoom além dos limites configurados).

## 4. Critérios de "Pronto para Lançamento" (Steam)

- [ ] Todos os itens da seção 3 passando.
- [ ] Métricas de progressão (seção 2) dentro da faixa desejada, validadas por playtest humano (não só simulação).
- [ ] Integração Steam (achievements, Cloud Save) testada em condições reais de rede instável (desconectar durante um autosave, por exemplo).
- [ ] Build empacotada via `jpackage` testada em uma máquina limpa (sem JDK instalado), nos sistemas operacionais que você pretende suportar.
- [ ] Textos em Inglês e Português revisados por um humano (não só traduzidos — revisados quanto a tom/clareza).

---

## Fim do Roadmap Inicial de Documentação

Os 10 documentos planejados no início da conversa estão completos:

1. GDD — Visão Geral
2. Economia & Balanceamento
3. Especificação dos Planetas
4. Sistema de Ascensão
5. Arquitetura Técnica
6. Modelo de Dados (Save)
7. Classe `BigNumber`
8. Especificação de Sistemas Core
9. UI/UX Spec & Roadmap de Implementação
10. Checklist de Balanceamento & Testes (este documento)

**Sugestão de próximo passo real:** começar pela Fase 0 do roadmap (doc 9) — setup do projeto LibGDX + `BigNumber` com testes — já que é a fundação de tudo o mais. Se quiser, posso te ajudar a montar o esqueleto de código dessa fase inicial quando você estiver pronto para sair da documentação e entrar na implementação.
