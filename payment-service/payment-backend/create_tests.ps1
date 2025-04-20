# Répertoires source et test
$srcDir = "src\main\java"
$testDir = "src\test\java"
$basePackagePath = "grapes\microservices\paymentbackend" # Chemin de base des packages

Write-Host "Starting test structure generation..."

# Chemin complet du répertoire source
$fullSrcDir = Join-Path $PSScriptRoot $srcDir # $PSScriptRoot est le dossier du script

# Vérifie si le répertoire source existe
if (-not (Test-Path $fullSrcDir)) {
    Write-Error "Error: Source directory '$fullSrcDir' not found."
    Write-Error "Please run this script from the root of your project."
    exit 1
}

# Trouve tous les fichiers .java dans le répertoire source
Get-ChildItem -Path (Join-Path $fullSrcDir $basePackagePath) -Filter *.java -Recurse | ForEach-Object {
    $srcFile = $_

    # Extrait le chemin relatif (par rapport à src/main/java)
    $relativePath = $srcFile.FullName.Substring($fullSrcDir.Length).TrimStart('\') # ex: grapes\...\ClientService.java
    $packagePath = Split-Path $relativePath -Parent                            # ex: grapes\...\services
    $className = $srcFile.BaseName                                             # ex: ClientService

    # Ignore l'application principale si besoin
    if ($className -eq "PaymentBackendApplication") {
        Write-Host "Skipping application class: $className"
        continue
    }

    # Construit le chemin du fichier de test
    $testClassName = "${className}Test"
    $testDirPath = Join-Path $PSScriptRoot $testDir $packagePath
    $testFilePath = Join-Path $testDirPath "${testClassName}.java"

    # Crée le répertoire de test s'il n'existe pas
    if (-not (Test-Path $testDirPath)) {
        New-Item -ItemType Directory -Path $testDirPath -Force | Out-Null
    }

    # Crée le fichier de test s'il n'existe pas déjà
    if (-not (Test-Path $testFilePath)) {
        Write-Host "Creating: $testFilePath"
        # Récupère le nom du package
        $packageName = $packagePath -replace '\\', '.'

        # Crée le fichier avec une structure de base
        $fileContent = @"
package $packageName;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
// Ajoutez d'autres imports communs ici si nécessaire
// import static org.mockito.Mockito.*;
// import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.junit.jupiter.MockitoExtension;

// @ExtendWith(MockitoExtension.class) // Décommentez si vous utilisez Mockito
class ${testClassName} {

    // Ajoutez des mocks ici si nécessaire avec @Mock
    // @Mock
    // private DependencyType dependency;

    // Ajoutez le sujet du test avec @InjectMocks
    // @InjectMocks
    // private ${className} serviceOrController;

    // Ajoutez une méthode @BeforeEach si nécessaire
    // @BeforeEach
    // void setUp() {
    // }

    @Test
    void exampleTest() {
        // TODO: Implémentez votre premier test ici
        // Arrange
        // Act
        // Assert
        // fail("Test non implémenté");
    }
}
"@
        # Utilise UTF8 pour l'encodage pour éviter les problèmes potentiels
        Out-File -FilePath $testFilePath -InputObject $fileContent -Encoding UTF8 -Force
    } else {
        Write-Host "Skipping (already exists): $testFilePath"
    }
}

Write-Host "Test structure generation finished."