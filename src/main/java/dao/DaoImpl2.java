package dao;

public class DaoImpl2 implements IDao {
    @Override
    public double getValue() {
        System.out.println("[DAO 2] Récupération de la valeur depuis la deuxième source de données...");
        return 250.0;
    }
}
