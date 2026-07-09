package com.example.alphadiet;

public class Producto {

    private String id;
    private String nombre;
    private String descripcion;
    private double precio;
    private String categoria;
    private String imagen;
    private int calorias;
    private int proteina;
    private int carbohidratos;
    private int grasas;

    public Producto() {}

    public Producto(String id, String nombre, String descripcion, double precio, String categoria, String imagen) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.categoria = categoria;
        this.imagen = imagen;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }

    public double getPrecio() { return precio; }
    public void setPrecio(double precio) { this.precio = precio; }

    public String getCategoria() { return categoria; }
    public void setCategoria(String categoria) { this.categoria = categoria; }

    public String getImagen() { return imagen; }
    public void setImagen(String imagen) { this.imagen = imagen; }

    public int getCalorias() { return calorias; }
    public void setCalorias(int calorias) { this.calorias = calorias; }

    public int getProteina() { return proteina; }
    public void setProteina(int proteina) { this.proteina = proteina; }

    public int getCarbohidratos() { return carbohidratos; }
    public void setCarbohidratos(int carbohidratos) { this.carbohidratos = carbohidratos; }

    public int getGrasas() { return grasas; }
    public void setGrasas(int grasas) { this.grasas = grasas; }
}