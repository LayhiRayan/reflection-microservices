package presentation;

import dao.IDao;
import metier.IMetier;
import metier.MetierImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.*;
import static org.junit.jupiter.api.Assertions.*;

class Presentation2Test {
    @TempDir Path dossier;
    private Path config(String contenu) throws Exception {
        return Files.writeString(dossier.resolve("config.txt"), contenu);
    }
    @Test void configurationDuTpRetourne200() throws Exception {
        assertEquals(200.0, Presentation2.charger(Paths.get("config.txt")).calcul());
    }
    @Test void changementDuDaoUniquementParConfiguration() throws Exception {
        assertEquals(500.0, Presentation2.charger(Paths.get("config2.txt")).calcul());
    }
    @Test void calculSansInjectionRefuse() {
        assertThrows(IllegalStateException.class, () -> new MetierImpl().calcul());
    }
    @Test void fichierAbsentSignale() {
        assertThrows(ConfigurationException.class, () -> Presentation2.charger(dossier.resolve("absent.txt")));
    }
    @Test void configurationIncompleteOuVideRefusee() throws Exception {
        for (String texte : new String[]{"", "dao.DaoImpl\n", "dao.DaoImpl\n\n", "dao.DaoImpl\nmetier.MetierImpl\nligneEnTrop"}) {
            Path fichier = config(texte);
            assertThrows(ConfigurationException.class, () -> Presentation2.charger(fichier));
        }
    }
    @Test void classeInconnueSignalee() throws Exception {
        Path fichier = config("dao.Absent\nmetier.MetierImpl");
        assertTrue(assertThrows(ConfigurationException.class, () -> Presentation2.charger(fichier)).getMessage().contains("Classe introuvable"));
    }
    @Test void contratIncorrectRefuse() throws Exception {
        Path fichier = config("java.lang.String\nmetier.MetierImpl");
        assertTrue(assertThrows(ConfigurationException.class, () -> Presentation2.charger(fichier)).getMessage().contains("dao.IDao"));
    }
    @Test void constructeurSansArgumentObligatoire() throws Exception {
        Path fichier = config(DaoSansConstructeur.class.getName() + "\nmetier.MetierImpl");
        assertTrue(assertThrows(ConfigurationException.class, () -> Presentation2.charger(fichier)).getMessage().contains("Constructeur public"));
    }
    @Test void setterObligatoire() throws Exception {
        Path fichier = config("dao.DaoImpl\n" + MetierSansSetter.class.getName());
        assertTrue(assertThrows(ConfigurationException.class, () -> Presentation2.charger(fichier)).getMessage().contains("setDao"));
    }
    @Test void causeDeLEchecInjectionConservee() throws Exception {
        Path fichier = config("dao.DaoImpl\n" + MetierRefusant.class.getName());
        ConfigurationException erreur = assertThrows(ConfigurationException.class, () -> Presentation2.charger(fichier));
        assertInstanceOf(IllegalArgumentException.class, erreur.getCause());
    }
    public static class DaoSansConstructeur implements IDao {
        public DaoSansConstructeur(int valeur) { }
        public double getValue() { return 1.0; }
    }
    public static class MetierSansSetter implements IMetier {
        public double calcul() { return 0.0; }
    }
    public static class MetierRefusant implements IMetier {
        public void setDao(IDao dao) { throw new IllegalArgumentException("Injection refusée"); }
        public double calcul() { return 0.0; }
    }
}
