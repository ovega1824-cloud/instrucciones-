# Instrucciones 2.5.0 (Forge 1.20.1)

Mod "Instrucciones" 2.4.2 + comando nuevo **/ins backgraund** (tambien funciona `/ins background`).

- Abre un menu con el fondo predeterminado del mod y todas las imagenes de la carpeta `.minecraft/instrucciones_fondos` (png, jpg).
- Al elegir una, se aplica a todos los jugadores que tengan esa imagen en su carpeta (los demas ven el fondo predeterminado).
- Botones: Abrir carpeta, Recargar, paginas, vista previa.
- Solo operadores (nivel 2), igual que el resto de `/ins`.
- Todos los comandos y la configuracion originales siguen igual (show, hide, set, image, timer, reset).

## Como se construye
1. Sube TODO este contenido a un repo de GitHub (incluida la carpeta `.github/workflows` y `original/`).
2. En la pestana **Actions** corre solo; al terminar, el .jar queda en **Releases** (y en Artifacts).
3. Pon el .jar en `mods/` (cliente Y servidor). Quita el instrucciones-2.4.2.jar viejo.
