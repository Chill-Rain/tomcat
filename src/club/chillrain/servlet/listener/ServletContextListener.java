package club.chillrain.servlet.listener;

/**
 * @author ChillRain 2023 08 20
 */
public interface ServletContextListener extends Listener{
    default void initServletContext(ServletContextEvent event){

    }
    default void destroyed(ServletContextEvent event){

    }
}
