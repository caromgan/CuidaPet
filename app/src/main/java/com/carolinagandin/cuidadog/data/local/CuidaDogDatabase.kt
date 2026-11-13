package com.carolinagandin.cuidadog.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        PerroEntity::class,
        CuidadoEntity::class,
        AlimentacionHistorialEntity::class,
        AlimentacionPruebaEntity::class,
        AlimentacionEvolucionEntity::class
    ],
    version = 9,
    exportSchema = false
)
abstract class CuidaDogDatabase : RoomDatabase() {
    abstract fun perroDao(): PerroDao
    abstract fun cuidadoDao(): CuidadoDao
    abstract fun alimentacionHistorialDao(): AlimentacionHistorialDao
    abstract fun alimentacionPruebaDao(): AlimentacionPruebaDao
    abstract fun alimentacionEvolucionDao(): AlimentacionEvolucionDao

    companion object {
        @Volatile
        private var INSTANCE: CuidaDogDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `cuidados` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `tipo` TEXT NOT NULL,
                        `titulo` TEXT NOT NULL,
                        `fecha` TEXT NOT NULL,
                        `hora` TEXT,
                        `tieneRepeticion` INTEGER NOT NULL DEFAULT 0,
                        `repeticionCantidad` INTEGER,
                        `repeticionUnidad` TEXT,
                        `notas` TEXT,
                        `valorPeso` REAL,
                        `realizado` INTEGER NOT NULL DEFAULT 0
                    )
                    """.trimIndent()
                )
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `fotoUri` TEXT")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `especie` TEXT NOT NULL DEFAULT 'Perro'")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `raza` TEXT NOT NULL DEFAULT ''")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `sexo` TEXT NOT NULL DEFAULT 'No especificado'")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `fechaNacimientoIso` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `edadAproximadaAnios` INTEGER")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `talla` TEXT NOT NULL DEFAULT 'Mediana'")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `pesoKg` REAL NOT NULL DEFAULT 0.0")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `color` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `castrado` INTEGER")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `numeroMicrochip` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `alergias` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `medicacionHabitual` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `veterinario` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `observaciones` TEXT")

                db.execSQL("ALTER TABLE `cuidados` ADD COLUMN `mascotaId` INTEGER NOT NULL DEFAULT 1")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_cuidados_mascotaId` ON `cuidados` (`mascotaId`)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `tipoDieta` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `marcaAlimento` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `cantidadPorRacion` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `racionesPorDia` INTEGER")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `horariosAlimentacion` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `restriccionesAlimentarias` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `urgencias24h` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `contactoPrincipal` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `contactoSecundario` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `contactoAlternativo` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `vacunasInfo` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `desparasitacionInfo` TEXT")
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `enfermedadesCronicas` TEXT")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `alimentacion_historial` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `mascotaId` INTEGER NOT NULL,
                        `fecha` TEXT NOT NULL,
                        `dietaTipo` TEXT NOT NULL,
                        `marcaAlimento` TEXT NOT NULL,
                        `cantidadPorRacion` TEXT NOT NULL,
                        `racionesPorDia` INTEGER NOT NULL,
                        `horarios` TEXT,
                        `restricciones` TEXT,
                        `observaciones` TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_alimentacion_historial_mascotaId` ON `alimentacion_historial` (`mascotaId`)")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `cuidados` ADD COLUMN `diasSemana` TEXT")
            }
        }

        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `alimentacion_pruebas` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `mascotaId` INTEGER NOT NULL,
                        `tipoDieta` TEXT NOT NULL,
                        `marcaAlimento` TEXT NOT NULL,
                        `cantidadPorRacion` TEXT NOT NULL,
                        `unidad` TEXT NOT NULL,
                        `racionesPorDia` INTEGER NOT NULL,
                        `horarios` TEXT,
                        `fechaInicio` TEXT NOT NULL,
                        `observaciones` TEXT,
                        `estado` TEXT NOT NULL
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_alimentacion_pruebas_mascotaId` ON `alimentacion_pruebas` (`mascotaId`)")

                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `alimentacion_evoluciones` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `pruebaId` INTEGER NOT NULL,
                        `mascotaId` INTEGER NOT NULL,
                        `fecha` TEXT NOT NULL,
                        `valoracion` TEXT NOT NULL,
                        `observacion` TEXT
                    )
                    """.trimIndent()
                )
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_alimentacion_evoluciones_pruebaId` ON `alimentacion_evoluciones` (`pruebaId`)")
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_alimentacion_evoluciones_mascotaId` ON `alimentacion_evoluciones` (`mascotaId`)")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `alimentacion_pruebas` ADD COLUMN `fechaFin` TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE `perro` ADD COLUMN `historialCelos` TEXT")
            }
        }

        fun getDatabase(context: Context): CuidaDogDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    CuidaDogDatabase::class.java,
                    "cuidadog_database"
                )
                    .addMigrations(
                        MIGRATION_1_2,
                        MIGRATION_2_3,
                        MIGRATION_3_4,
                        MIGRATION_4_5,
                        MIGRATION_5_6,
                        MIGRATION_6_7,
                        MIGRATION_7_8,
                        MIGRATION_8_9
                    )
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
