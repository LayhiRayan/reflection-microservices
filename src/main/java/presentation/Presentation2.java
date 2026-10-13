package presentation;

import dao.IDao;
import metier.IMetier;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.List;

public class Presentation2 {
    public static void main(String[] args) {
        if (args.length > 1) {
            System.err.println("Usage : presentation.Presentation2 [chemin/config.txt]");
            System.exit(1);
        }
        try {
            Path fichier = Paths.get(args.length == 0 ? "config.txt" : args[0]);
            IMetier metier = charger(fichier);
            System.out.println("Résultats = " + metier.calcul());
        } catch (ConfigurationException | RuntimeException e) {
            System.err.println("[ERREUR] " + e.getMessage());
            System.exit(1);
        }
    }

    public static IMetier charger(Path fichier) throws ConfigurationException {
        List<String> lignes;
        try {
            lignes = Files.readAllLines(fichier, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new ConfigurationException("Impossible de lire la configuration : " + fichier.toAbsolutePath(), e);
        }
        if (lignes.size() != 2 || lignes.stream().anyMatch(String::isBlank)) {
            throw new ConfigurationException("config.txt doit contenir exactement deux lignes non vides : classe DAO puis classe métier.");
        }
        IDao dao = instancier(lignes.get(0).trim(), IDao.class);
        System.out.println("[CONTAINER] DAO instancié : " + dao.getClass().getName());
        IMetier metier = instancier(lignes.get(1).trim(), IMetier.class);
        System.out.println("[CONTAINER] Métier instancié : " + metier.getClass().getName());
        try {
            Method setter = metier.getClass().getMethod("setDao", IDao.class);
            setter.invoke(metier, dao);
        } catch (NoSuchMethodException e) {
            throw new ConfigurationException("La classe métier doit exposer public void setDao(IDao dao).", e);
        } catch (IllegalAccessException e) {
            throw new ConfigurationException("La méthode setDao doit être accessible publiquement.", e);
        } catch (InvocationTargetException e) {
            throw new ConfigurationException("Échec de l'injection : " + e.getCause(), e.getCause());
        }
        System.out.println("[CONTAINER] Dépendance injectée via setDao(IDao).");
        return metier;
    }

    private static <T> T instancier(String nom, Class<T> contrat) throws ConfigurationException {
        try {
            // Seuls les constructeurs publics sont utilisés ; aucun contournement des accès.
            Class<? extends T> classe = Class.forName(nom).asSubclass(contrat);
            return classe.getConstructor().newInstance();
        } catch (ClassNotFoundException e) {
            throw new ConfigurationException("Classe introuvable : " + nom, e);
        } catch (ClassCastException e) {
            throw new ConfigurationException(nom + " doit implémenter " + contrat.getName(), e);
        } catch (NoSuchMethodException e) {
            throw new ConfigurationException("Constructeur public sans argument manquant : " + nom, e);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ConfigurationException("Classe non instanciable ou non accessible : " + nom, e);
        } catch (InvocationTargetException e) {
            throw new ConfigurationException("Échec du constructeur de " + nom + " : " + e.getCause(), e.getCause());
        }
    }
}
