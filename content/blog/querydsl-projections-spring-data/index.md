---
title: QueryDSL projections with Spring Data JPA
date: "2019-11-30T12:12:03.284Z"
description: "Spring Data JPA does not support QueryDSL projections by default, but it's quite easy to add support for them manually. Let's see how it can be implemented."
---

### QueryDSL projections

QueryDSL projections are simple DTO classes that represent a group of properties to fetch from a database. Projections
may be useful when we need only a small subset of columns from the queried entities. In such cases, they help us reduce the traffic between the database and our application.

### Spring Data JPA + QueryDSL

Spring Data JPA provides support for the QueryDSL library via the `QuerydslPredicateExecutor` interface.

`embed:querydsl-predicate-executor.java`

The only required step is to create a repository that extends the executor interface.

`embed:example-repo-querydsl-executor.java`

From the interface definition, we can clearly see that projections are not supported - there is only one type
parameter for the main entity class. Yet, we can quite easily extend the executor interface and provide support
for projections and more sophisticated QueryDSL queries. 

### Adding projections

Let's start by defining an interface for extended QueryDSL executor. The new API will allow us to return projections
and also to provide any QueryDSL queries as a method parameter.

`embed:querydsl-ext-executor.java`

We can now change our repository to extend from the new executor interface.

`embed:user-repository.java`

But how to implement the generation of the new methods in our repositories?
It's actually quite easy. The starting point will be the `JpaRepositoryFactoryBean` class.
Let's look at the class documentation:
```java
/**
 * Special adapter for Springs {@link org.springframework.beans.factory.FactoryBean} 
 * interface to allow easy setup of repository factories via Spring configuration.
 */
```

We need to override the `createRepositoryFactory` to provide our implementation of the `JpaRepositoryFactory`
class when the repository interface extends our custom QueryDSL executor.

The example implementation may look something like this:

`embed:factory-bean-impl.java`

Now, we have to implement the `QuerydslJpaBaseRepository` class. It will be the base class for all the repositories that extends the `ExtendedQuerydslPredicateExecutor`. The easiest way to do that is to extend
the Spring `SimpleJpaRepository` and implement only the methods from the executor interface. We can also reuse
the existing implementation of the Spring QueryDSL executor. The final implementation of the base repository
may look something like:

`embed:querydsl-base-repo.java`

That's all, we are done with the implementation. The only missing step is to set the `repositoryFactoryBeanClass`
in the `@EnableJpaRepositories` annotation. 

```java
@EnableJpaRepositories(repositoryFactoryBeanClass = QuerydslJpaRepositoryFactoryBean.class)
@SpringBootApplication
class Application ...
```

### Example usage

Let's define a simple JPA entity representing the user of our application and a projection class that contains
only the subset of the entity properties.

`embed:user-entity.java`

`embed:user-projection.java`

We can now use our `UserRepository` instance to get a page of projection objects:

```java
    Page<UserProjection> getFirstNameLikePage(String firstNameLike, Pageable pageable) {
        QUser user = QUser.user;
        return userRepository.findAll(
                Projections.constructor(UserProjection.class, user.id, user.username),
                user.firstName.like(firstNameLike),
                pageable
        );
    }
```

The important thing here is the generated SQL query:

```sql
    select
        user0_.id as col_0_0_,
        user0_.user_name as col_1_0_ 
    from
        users user0_ 
    where
        user0_.first_name like ? escape '!' limit ?
```

As you can see only the columns from the projection are fetched during SQL query - that's what we wanted
to achieve.

Our new API gives us possibilities not only to fetch a projection of a single JPA entity, but we can also
create a projection of some more complex queries. 
Our second entity will be a subscription assigned to the user.

`embed:subscription-entity.java`

The subscription projection will contain the subscription name and username of an assigned user.

```java
@Getter
@EqualsAndHashCode
@RequiredArgsConstructor
public class SubscriptionProjection {
    private final String subscriptionName;
    private final String username;
}
```

We can now use the `UserRepository` instance to get the subscription data.
```java
    List<SubscriptionProjection> getAllSubscriptions() {
        QUser user = QUser.user;
        QSubscription subscription = QSubscription.subscription;
        JPQLQuery<SubscriptionProjection> query = queryBuilder
                .createQuery()
                .select(Projections.constructor(SubscriptionProjection.class, 
                        subscription.name, user.username))
                .from(user)
                .innerJoin(subscription)
                .on(user.id.eq(subscription.userId));
        return userRepository.findAll(query);
    }
```

The generated SQL query is:

```sql
    select
        subscripti1_.name as col_0_0_,
        user0_.user_name as col_1_0_ 
    from
        users user0_ 
    inner join
        subscription subscripti1_ 
            on (
                user0_.id=subscripti1_.user_id
            )
```


Great, we are now able to execute very complex QueryDSL queries without losing the comfortable Spring Data API like `Pageable`.
Success !


The full example code can be found at: https://github.com/michalperlak/querydsl-spring-data-projections




