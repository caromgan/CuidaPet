# CuidaDog - Especificación Funcional (SPEC.md)

## 1. Visión General
CuidaDog es una aplicación Android nativa para organizar los cuidados cotidianos de un perro. Funciona 100% de forma local y offline, sin requerir registro, servidor ni conexión a internet.

### Exención de Responsabilidad
CuidaDog es una herramienta de organización y recordatorio. No diagnostica enfermedades, no recomienda medicamentos ni dosis, no modifica indicaciones veterinarias y no reemplaza la consulta con un veterinario.

### Preguntas clave que responde:
- ¿Qué necesita mi perro hoy?
- ¿Qué cuidados vienen próximamente?
- ¿Cuándo fue la última vez que hice determinado cuidado?

---

## 2. Primera Apertura y Perro (MVP)
- **Alcance:** Soporte para **1 solo perro** por aplicación.
- **Flujo de primera apertura:** Si aún no existe un perro registrado, la aplicación muestra primero la pantalla de creación del perfil del perro. Una vez guardado el nombre, ingresa a la pantalla **Hoy**.
- **Datos del Perro:**
  - Nombre (Obligatorio)
  - Foto (Opcional)
  - Fecha de nacimiento / Edad (Opcional)
  - Raza (Opcional)
  - Peso actual (Opcional, actualizado desde los registros de tipo Peso)
  - Datos de veterinario y contacto (Opcional)

---

## 3. Ciclo Central del Producto
1. **Crear Cuidado:** El usuario registra un cuidado indicando tipo, título, fecha, hora (opcional), repetición (opcional) y notas (opcional).
2. **Programar / Notificar:** Se programa la notificación local (a la hora exacta o un aviso general diario si solo tiene fecha).
3. **Mostrar en 'Hoy':** Se visualiza en el día correspondiente. Si la fecha ya pasó y no se marcó como realizado, se muestra en la sección de **"Atrasados"**.
4. **Acciones del Usuario:**
   - **Marcar como Realizado:** Se registra la ejecución en el **Historial**. Si tiene repetición, se calcula la siguiente fecha a partir de la fecha programada originalmente.
   - **Posponer:** Permite seleccionar una nueva fecha y (si corresponde) nueva hora.
   - **Editar:** Permite modificar los datos de un cuidado existente.
   - **Eliminar:** Requiere confirmación previa. Eliminar un cuidado futuro no borra los registros históricos pasados ya realizados.
5. **Próximamente:** Muestra las siguientes ocurrencias ordenadas por horizonte temporal.

---

## 4. Pantallas del MVP (5 Pantallas)

### 1. Hoy
- Lista de cuidados correspondientes al día.
- Sección destacada para cuidados **Atrasados** de días anteriores no completados.
- **Estado vacío:** Si no hay cuidados programados, muestra un mensaje indicando que no hay cuidados pendientes y un botón "Agregar primer cuidado".
- Acciones: Marcar realizado, Posponer, Editar, Eliminar, Agregar cuidado.

### 2. Agregar / Editar Cuidado
- Formulario de creación y edición.
- Tipos de cuidado: Medicamento, Vacuna, Pipeta, Desparasitación, Baño, Peluquería, Control veterinario, Peso, Otro.

### 3. Próximamente
- Organización temporal: Esta semana, Próximos 30 días, Próximos 90 días, Más adelante.

### 4. Historial
- Registro histórico de cuidados realizados.
- Respuesta a "¿Cuándo fue la última vez que...?".
- Muestra fechas, horas de realización, notas registradas e historial de mediciones de peso.

### 5. Mi Perro
- Ficha de información básica del perro y botón de edición.

---

## 5. Validaciones Mínimas y Reglas de Negocio
- **Perro:** Nombre obligatorio.
- **Cuidado:** Tipo, Título y Fecha obligatorios.
- **Hora:** Opcional.
- **Notas:** Opcionales (texto libre).
- **Peso:** Si el tipo es "Peso", el valor numérico del peso es obligatorio.
- **Repetición:** Si se activa repetición, la cantidad (número) y la unidad (días, semanas, meses) deben ser válidas.
- **Eliminación:** Confirmación obligatoria antes de eliminar. Borrar un cuidado programado preserva los registros históricos ya completados.

---

## 6. Criterios de Aceptación (DADO / CUANDO / ENTONCES)

### Crear perro
- **DADO** que la app no tiene un perro registrado, **CUANDO** el usuario ingresa un nombre válido y guarda, **ENTONCES** se crea el perfil y navega a la pantalla 'Hoy'.

### Crear cuidado
- **DADO** que el usuario está en el formulario de agregar cuidado, **CUANDO** completa tipo, título y fecha obligatorios y guarda, **ENTONCES** el cuidado queda registrado y programado.

### Mostrar cuidado en Hoy
- **DADO** un cuidado programado para la fecha actual, **CUANDO** el usuario abre la pantalla 'Hoy', **ENTONCES** el cuidado aparece listado en las tareas del día.

### Marcar realizado
- **DADO** un cuidado pendiente en 'Hoy', **CUANDO** el usuario lo marca como realizado, **ENTONCES** se mueve al 'Historial' con la fecha/hora actual de realización.

### Guardar en Historial
- **DADO** un cuidado marcado como realizado, **CUANDO** el usuario consulta la pantalla 'Historial', **ENTONCES** figura registrado de forma cronológica.

### Calcular próxima fecha
- **DADO** un cuidado recurrente marcado como realizado, **CUANDO** se completa la acción, **ENTONCES** se genera automáticamente la próxima ocurrencia sumando la frecuencia a la fecha programada original.

### Mostrar atrasado
- **DADO** un cuidado con fecha anterior a hoy no marcado como realizado, **CUANDO** el usuario abre 'Hoy', **ENTONCES** aparece resaltado en la sección "Atrasados".

### Posponer
- **DADO** un cuidado pendiente o atrasado, **CUANDO** el usuario elige posponer y selecciona nueva fecha/hora, **ENTONCES** se re-programa para ese nuevo momento.

### Editar
- **DADO** un cuidado existente, **CUANDO** el usuario modifica sus datos y guarda, **ENTONCES** los cambios se reflejan en las pantallas correspondientes.

### Eliminar
- **DADO** un cuidado seleccionado para eliminar, **CUANDO** el usuario confirma la eliminación en el diálogo, **ENTONCES** se borra el cuidado programado sin alterar los registros pasados en el Historial.

### Registrar peso
- **DADO** un cuidado de tipo "Peso" con valor numérico, **CUANDO** se marca como realizado, **ENTONCES** se actualiza el peso actual en 'Mi Perro' y se añade la medición al Historial.

---

## 7. Casos a Considerar (Estados Vacíos, Validaciones y Errores)
- **Estado vacío en 'Hoy':** Mensaje claro + botón "Agregar primer cuidado".
- **Estado vacío en 'Próximamente':** Mensaje indicando que no hay cuidados futuros agendados.
- **Estado vacío en 'Historial':** Mensaje indicando que aún no hay cuidados realizados registrados.
- **Intentar guardar sin campos obligatorios:** Deshabilitar botón de guardar o mostrar mensaje de error en el campo faltante (ej. Nombre de perro, Título de cuidado, Valor de peso).
- **Intento de registrar peso con letras o valor negativo:** Validación de campo numérico positivo.

---

## 8. Fuera de Alcance del MVP
- Múltiples perros
- Login / Cuentas de usuario
- Backend
- Firebase
- Nube / Cloud storage
- Sincronización familiar o entre dispositivos
- Marketplace
- Veterinarías conectadas
- Paseadores
- GPS / Rastreo
- Comunidad / Red social
- Tienda de productos
- Control de gastos
- Alimentación
- Entrenamiento
- Diagnóstico veterinario
- Recomendaciones médicas
- Chatbot
- Inteligencia Artificial (IA) dentro de CuidaDog
- Gráficos de evolución de peso

---

## 9. Evoluciones Futuras (Post-MVP)
- Soporte para múltiples perfiles de perros.
- Gráficos e indicadores de evolución de peso.
- Exportación e importación de datos en PDF o CSV para compartir con el veterinario.
- Categorización de gastos y recordatorio de compras de alimento/insumos.
- Copia de seguridad local (Backup/Restore).
- Tema oscuro / claro personalizable.
