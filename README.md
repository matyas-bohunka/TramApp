# TramTire - Bemutató README

## Alkalmazás áttekintése

A TramTire egy Android alkalmazás, amely a villamosok kerekének mérésére szolgál. Az alkalmazás segítségével rögzítheti a kerékméréseket, mentheti el őket, és exportálhatja az adatokat Excel fájlba.

## Főképernyő

Az alkalmazás megnyitásakor a főképernyő fogadja Önt, ahol három gomb található egymás alatt:

[Főképernyő] *(Kép helye a főképernyőről)*

1.  **Új mérés:** Új mérés indításához kattintson erre a gombra.
2.  **Mérések:** A korábbi mérések listájának megtekintéséhez kattintson erre a gombra.
3.  **Új jármű felvétele:** Új villamostípus hozzáadásához kattintson erre a gombra.

## Új mérés képernyő

Az "Új mérés" gombra kattintva a következő képernyő jelenik meg:

[Új mérés képernyő].  *(Kép helye az "Új mérés" képernyőről)*

1.  **Válassz jármű típust:**  Egy legördülő listából választhatja ki a villamos típusát. Ha még nincs járműtípus felvéve, akkor először vegyen fel egyet az "Új jármű felvétele" menüpontban.
2.  **Pályaszám:** Írja be a villamos pályaszámát. Ez betűket és számokat is tartalmazhat.
3.  **Dátum:** Kattintson a "Dátum kiválasztása" gombra a dátumválasztó megnyitásához. Válassza ki a mérés dátumát. A kiválasztott dátum a gomb mellett fog megjelenni.
4.  **Mégse gomb:** A "Mégse" gombra kattintva visszatér a főképernyőre anélkül, hogy bármit is elmentene.
5.  **Ok gomb:** Az "Ok" gombra kattintva az alkalmazás ellenőrzi, hogy kiválasztott-e járműtípust, dátumot, és hogy a pályaszám mező nem üres-e. Ha minden rendben van, megnyílik a "Kerékmérés" képernyő.

## Kerékmérés képernyő

Az "Ok" gombra kattintás után a "Kerékmérés" képernyő jelenik meg, amely a kiválasztott villamostípus tengelyeit és kerekeit mutatja:

[Kerékmérés képernyő]  *(Kép helye a "Kerékmérés" képernyőről)*

*   **Tengelyek:** A képernyő közepén vízszintes vonalak jelzik a tengelyeket. A vonalak felett a tengely száma látható.
*   **Kerekek:** Minden tengely mellett két gomb található, amelyek a kerekek számát jelzik. Bal oldalon a páratlan számú, jobb oldalon a páros számú kerekek találhatók. Például az első tengely bal oldali kereke az 1. kerék, a jobb oldali a 2. kerék, a második tengely bal oldali kereke a 3. kerék, és így tovább.
*   **Mérés bevitel:**
    *   Kattintson egy kerékgombra a mérésbevitel ablak megnyitásához.
    *   Egy felugró ablakban 3 szövegmezőbe írhatja be a kerék három mérését (tört számokat vesszővel használjon).
    *   Kattintson a "Mégse" gombra a mérés elvetéséhez és az ablak bezárásához.
    *   Kattintson az "Ok" gombra a mérések átlagának kiszámításához és mentéséhez.
*   **Mérés kijelzése:**
    *   Sikeres mérés után a kerékgomb melletti szövegben megjelenik a mért átlagérték.
    *   A kerékgomb melletti jelölőnégyzet be lesz jelölve, jelezve, hogy a kerék mérése megtörtént.
*   **Mérés módosítása:** A kerékgombra újra kattintva módosíthatja a korábbi mérést.
*   **Vissza gomb:** A "Mégse" gombra kattintva visszatérhet az "Új mérés" képernyőre. A kerékmérések nem lesznek elmentve.
*   **Mentés gomb:** Ha minden kerékhez beírta a mérést (minden jelölőnégyzet be van jelölve), akkor az "Ok" gombra kattintva elmentheti a mérést. A program visszatér a főképernyőre, és megjelenik egy "Sikeres mérés" üzenet.

## Mérések képernyő

A főképernyőn a "Mérések" gombra kattintva a korábbi mérések listája jelenik meg:

[Mérések képernyő]  *(Kép helye a "Mérések" képernyőről)*

*   **Mérés gombok:** A képernyőn gombok listája található, amelyek a korábbi méréseket jelzik. A gombok neve a villamos pályaszáma és a mérés dátuma.
*   **Görgethető lista:** A lista görgethető, ha sok mérés van rögzítve.
*   **Vissza gomb:** A "Vissza" gombra kattintva visszatérhet a főképernyőre.
*   **Mérés részletei:** Kattintson egy mérés gombra a mérés részleteinek megtekintéséhez.

## Mérés részletei képernyő

Egy mérés gombra kattintva a "Mérések" képernyőn a mérés részletei jelennek meg:

[Mérés részletei képernyő].  *(Kép helye a "Mérés részletei" képernyőről)*

*   **Mérés adatai:** A képernyőn tengelyenként és kerékenként láthatók a mért értékek, a kerék száma, és a tengely száma.
*   **Mégse gomb:** A "Mégse" gombra kattintva visszatérhet a "Mérések" képernyőre.
*   **Excel importálás gomb:** Az "Excel importálás" gombra kattintva elmentheti a méréseket Excel fájlba. A fájl mentési helyét és nevét Ön választhatja ki.

## Új jármű felvétele képernyő

A főképernyőn az "Új jármű felvétele" gombra kattintva az új villamostípus felvételéhez szükséges képernyő jelenik meg:

[Új jármű felvétele képernyő]  *(Kép helye az "Új jármű felvétele" képernyőről)*

1.  **Jármű típusa:** Írja be az új villamos típusának nevét.
2.  **Tengelyek száma:** Írja be az új villamostípus tengelyeinek számát (számmal).
3.  **Mégse gomb:** A "Mégse" gombra kattintva visszatérhet a főképernyőre anélkül, hogy új járműtípust mentene el.
4.  **Ok gomb:** Az "Ok" gombra kattintva az alkalmazás ellenőrzi, hogy mindkét mező ki van-e töltve. Ha igen, elmenti az új villamostípust, és visszatér a főképernyőre egy "Sikeres jármű felvétel" üzenettel.

---

**Megjegyzés:** A képernyőképek helyére kérjük, helyezze be az alkalmazás tényleges képernyőképeit a jobb bemutatás érdekében.
