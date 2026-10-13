package metier;

import dao.IDao;
import java.util.Objects;

public class MetierImpl implements IMetier {
    private IDao dao;

    public void setDao(IDao dao) {
        this.dao = Objects.requireNonNull(dao, "Le DAO ne doit pas être nul.");
    }

    @Override
    public double calcul() {
        if (dao == null) throw new IllegalStateException("Le DAO n'a pas été injecté.");
        double resultat = dao.getValue() * 2;
        System.out.println("[METIER] Calcul effectué. Résultat : " + resultat);
        return resultat;
    }
}
