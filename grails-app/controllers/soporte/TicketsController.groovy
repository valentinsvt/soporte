package soporte

import groovy.time.TimeCategory
import soporte.seguridad.Shield

class TicketsController  extends Shield{
    def ticketsService
    def index() {
        redirect(action: 'lista')
    }
    def nuevo(){
        def max = new Date()
        def min = new Date()
        use( TimeCategory ) {
            max = max + 1.hours
            min = max-1.weeks
        }
        println "max "+max
        [max:max,min:min]
    }

    def save_ajax(){
        def cliente = Cliente.findByCodigo(params.cliente_codigo)
        def ticket = new Ticket(params)
        if(params.id){
            ticket=Ticket.get(params.id)
            ticket.properties=params
        }
        ticket.cliente=cliente
        ticket.fuente="W"
        ticket.ip=session.ip
        if(!ticket.estado){
            ticket.estado=Estado.findByCodigo("P01")
        }
        if(!ticket.usuario){
            ticket.usuario=session.usuario
        }
        if(!ticket.save(flush: true))
            println "error save ticket "+ticket.errors
        redirect(action: 'verTicket',id: ticket.id)
    }

    def verTicket(){
        def ticket = Ticket.get(params.id)
        def fechaLimite=ticket.fecha
        use( TimeCategory ) {
            fechaLimite = fechaLimite + ticket.categoria.tiempo.hours
        }
        def acciones = AccionTomada.findAllByTicket(ticket,[sort:"fecha"])

        [ticket:ticket,fechaLimite:fechaLimite,accciones:acciones]
    }

    def saveAccion_ajax(){
        def accion = new AccionTomada()
        accion.usuario=session.usuario
        accion.fecha=new Date()
        accion.resumen=params.resumen
        accion.ticket=Ticket.get(params.ticket)
        accion.save(flush: true)
        if(params.bandera && params.bandera=="1") {
            accion.ticket.estado = Estado.findByCodigo("P02")
            ticketsService.cerrarTicket(accion.ticket)
            accion.ticket.save(flush: true)

        }
        redirect(action: "verTicket",id: accion.ticket.id)
    }

    def comboCategoria_ajax(){
        println "combo "+params
        def ops = Categoria.findAllByTipo(params.tipo)
        [ops:ops]
    }
    def cambiarEstado_ajax(){
        def ticket = Ticket.get(params.id)
        if(params.estado=="-1"){
            ticket.estado=Estado.findByCodigo("P01")
            ticketsService.abrirTicket(ticket)
        }
        if(params.estado=="1"){
            ticket.estado=Estado.findByCodigo("P02")
            ticketsService.cerrarTicket(ticket)
        }
        ticket.save(flush: true)
        redirect(action: "verTicket",id: params.id)
    }

    def lista(){
        def abiertos = Ticket.findAllByEstado(Estado.findByCodigo("P01"))
        abiertos=abiertos.sort{it.prioridad.alerta}
        [abiertos:abiertos]
    }

    def lista_ajax(){
        println "params "+params
        def cliente = null
        if(params.cliente!="")
            cliente=Cliente.findByCodigo(params.cliente)
        def categoria = null
        if(params.categoria!="")
            categoria=Categoria.get(params.categoria)
        def estado = null
        if(params.estado!="")
            estado=Estado.get(params.estado)
        def tickets = Ticket.withCriteria {
            if(params.cliente!=""){
                eq("cliente",cliente)
            }
            if(params.categoria!=""){
                eq("categoria",categoria)
            }
            if(params.estado!=""){
                eq("estado",estado)
            }
            if(params.descripcion!=""){
                ilike("descripcion","%"+params.descripcion+"%")
            }
        }
        [tickets:tickets]
    }



}
