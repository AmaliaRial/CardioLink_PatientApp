package pojos;

import java.util.Objects;

public class Symptoms {
    private int idSymptom;
    private String nameSymptom;

    public Symptoms(int idSymptom, String nameSymptom, String descriptionSymptom) {
        this.idSymptom = idSymptom;
        this.nameSymptom = nameSymptom;
    }

    public int getIdSymptom() {
        return idSymptom;
    }
    public void setIdSymptom(int idSymptom) {
        this.idSymptom = idSymptom;
    }
    public String getNameSymptom() {
        return nameSymptom;
    }
    public void setNameSymptom(String nameSymptom) {
        this.nameSymptom = nameSymptom;
    }


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Symptoms symptom = (Symptoms) o;
        return idSymptom == symptom.idSymptom && nameSymptom.equals(symptom.nameSymptom);
    }

    @Override
    public int hashCode() {
        return Objects.hash(idSymptom, nameSymptom);
    }

    @Override
    public String toString() {
        return "Symptoms{" +
                "idSymptom=" + idSymptom +
                ", nameSymptom='" + nameSymptom + '\'' +
                '}';
    }
}
