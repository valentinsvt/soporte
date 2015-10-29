package soporte

class Categoria {

    String descripcion
    String codigo
    String tipo

    static auditable = [ignore: []]

    /**
     * Define el mapeo entre los campos del dominio y las columnas de la base de datos
     */
    static mapping = {

        table 'spt_categoria'
        cache usage: 'read-write', include: 'non-lazy'
        version false
        columns {
            id column:'id'
            descripcion column: 'descripcion'
            codigo column: 'codigo'
            tipo column: 'tipo'
        }
    }


    static constraints = {
        descripcion(size: 1..100)
        codigo(size: 1..5)
        tipo(size: 1..1,nullable: true,blank: true)
    }
    String toString(){
        return this.descripcion
    }
}
