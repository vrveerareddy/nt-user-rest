package com.notempo1320.cli;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.module.mrbean.MrBeanModule;
import com.google.common.base.Optional;
import com.hubspot.dropwizard.guice.GuiceBundle;
import com.notempo1320.inject.AppModule;
import com.notempo1320.configuration.AppConfiguration;
import com.notempo1320.db.GenericDAO;
import com.notempo1320.db.hibernate.HibernateConfig;
import com.notempo1320.db.hibernate.UserHibernateDAO;
import com.notempo1320.facade.UserFacade;
import com.notempo1320.model.User;
import org.hibernate.SessionFactory;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.db.DataSourceFactory;
import io.dropwizard.hibernate.HibernateBundle;
import java.io.*;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.hibernate.cfg.AnnotationConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CreateUserCommand extends ConfiguredCommand<AppConfiguration> {

    private static final Logger LOGGER =
        LoggerFactory.getLogger(CreateUserCommand.class);

    private GuiceBundle<AppConfiguration> guiceBundle;

    public CreateUserCommand() {
        super("create_user", "Create a user that can access the app");


    }

    @Override
    public void configure(Subparser subparser) {
        super.configure(subparser);
        subparser.addArgument("-u", "--username")
                 .help("admin username");
    }


    @Override
    protected void run(Bootstrap<AppConfiguration> bootstrap,
                       Namespace namespace,
                       AppConfiguration configuration) {
        AnnotationConfiguration dbConfig = null;
        SessionFactory factory = null;
        try {
            dbConfig = HibernateConfig.getConfig(configuration);
            dbConfig.addAnnotatedClass(User.class);
            factory = dbConfig.buildSessionFactory();
            final GenericDAO<User> dao =
                new UserHibernateDAO(factory);
            final UserFacade facade = new UserFacade(dao);

            String username = namespace.getString("username");
            String pass1;
            String pass2;
            User obj = new User();

            factory.getCurrentSession().beginTransaction();
            System.out.print("\n Creating user: \n");
            System.out.print("\n type your username: \n");
            BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
            obj.setUsername(in.readLine());

            System.out.print("\n type your email: ");
            in = new BufferedReader(new InputStreamReader(System.in));
            obj.setEmail(in.readLine());

            obj.setToken(obj.randomUUID().toString());

            User createdUser = dao.create(obj);
            factory.getCurrentSession().getTransaction().commit();
            System.out.print("\nUser successfully created:\n");

            StringWriter writer = new StringWriter();
            bootstrap.getObjectMapper().writeValue(writer, createdUser);
            System.out.print(writer.toString());
            System.out.print("\n");
            System.exit(0);
        } catch (Exception ex) {
            ex.printStackTrace();
            factory.getCurrentSession().getTransaction().rollback();
            System.exit(1);
        }

    }

}
