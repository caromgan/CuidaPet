# Atribuciones de Fotografías - CuidaDog

## Estado Actual
Se utilizan contenedores visuales de reemplazo (fallback) mediante el sistema de diseño de Material 3 (`primaryContainer` + icono vectorial/emoji `🐾`) para evitar el uso de imágenes sin licencia o no verificables.

## Fotografías Pendientes de Incorporación
Cuando se disponga de fotografías con licencia verificable (ej. Unsplash / Pexels), se incorporarán bajo los siguientes nombres en `res/drawable/`:

1. **`perro_bienvenida_hero.jpg`**
   - **Ubicación:** Encabezado visual de `CrearPerroScreen.kt`
   - **Dimensiones esperadas:** 120dp x 120dp
   - **Uso:** Decorativo (`contentDescription = null`)

2. **`perro_hoy_hero.jpg`**
   - **Ubicación:** Hero banner de `HoyScreen.kt`
   - **Dimensiones esperadas:** Banner superior (ancho completo x 140dp)
   - **Uso:** Decorativo (`contentDescription = null`)
