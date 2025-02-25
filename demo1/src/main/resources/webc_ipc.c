#include <stdio.h>
#include <stdlib.h>
#include <stdint.h>
#include <string.h>
#include <unistd.h>
#include <sys/socket.h>
#include <linux/un.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <sys/time.h>
#include <fcntl.h>
#include <pwd.h>
#include <grp.h>
#include <pthread.h>
#include <signal.h>

#define PATH_SIZE 108
#define CLIENT_TIMEOUT 600
#define SOCK_BUF_SIZE 2048
#define SERVER_SOCK_FILE "/tmp/.webc_server"
#define CLIENT_SOCK_FILE "/tmp/.web_spring"
#define SELECT_TIMEOUT 5
#define DATA_SIZE 1024

#define BYTE_ALIGNED __attribute__((packed))

typedef struct
{
    int command;
    int status;
} BYTE_ALIGNED command_t;

typedef struct
{
    command_t header;
    struct sockaddr_un clnt_addr;
    char data[DATA_SIZE];
} BYTE_ALIGNED webc_message_t;

typedef struct _QueueEntry QueueEntry;
typedef struct _Queue Queue;

struct _QueueEntry
{
    QueueEntry *prev;
    QueueEntry *next;
    webc_message_t msg;
};

struct _Queue
{
    pthread_mutex_t queue_mutex;
    QueueEntry *head;
    QueueEntry *tail;
};

static struct sockaddr_un serv_addr;
Queue *queue = NULL;

Queue *queue_new(void)
{
    Queue *queue;

    queue = (Queue *)malloc(sizeof(Queue));

    if (queue == NULL)
    {
        return NULL;
    }
    // mutexInit(&(queue->queue_mutex), NULL);

    queue->head = NULL;
    queue->tail = NULL;

    return queue;
}

void *ipc_read()
{
    struct sockaddr_un unknownaddr;
    webc_message_t *rmsg = (webc_message_t *)malloc(sizeof(webc_message_t));
    int sockfd = socket(AF_UNIX, SOCK_STREAM, 0);
    struct timeval select_timeout;
    fd_set select_fd;
    int retval, select_ret;
    char buf[SOCK_BUF_SIZE] = {
        0,
    };
    socklen_t addr_len = sizeof(struct sockaddr_un);

    memset(&serv_addr, 0, addr_len);
    serv_addr.sun_family = AF_UNIX;
    snprintf(serv_addr.sun_path, sizeof(serv_addr.sun_path), "%s", SERVER_SOCK_FILE);

    if (SERVER_SOCK_FILE)
        unlink(SERVER_SOCK_FILE);

    if (bind(sockfd, (struct sockaddr *)&serv_addr, addr_len) != 0)
    {
        printf("Could not bind IPC socket.\n");
        goto exit;
    }

    if (chmod(SERVER_SOCK_FILE, 0755) < 0)
        printf("Failed to change %s permission.\n", SERVER_SOCK_FILE);

    while (!end_webc)
    {
        FD_ZERO(&select_fd);
        FD_SET(sockfd, &select_fd);
        select_timeout.tv_sec = SELECT_TIMEOUT;
        select_timeout.tv_usec = 0;
        select_ret = select(sockfd + 1, &select_fd, NULL, NULL, &select_timeout);

        if (select_ret > 0)
        {
            memset(buf, 0, SOCK_BUF_SIZE);
            memset(rmsg, 0, sizeof(webc_message_t));
            memset(&unknownaddr, 0, addr_len);
            retval = recvfrom(sockfd, buf, SOCK_BUF_SIZE, 0, (struct sockaddr *)&unknownaddr, &addr_len);
            if (retval < 0)
            {
                continue;
            }
            buf[SOCK_BUF_SIZE - 1] = '\0';

            memset(&rmsg->clnt_addr, 0, addr_len);
            memcpy(&rmsg->clnt_addr, &unknownaddr, addr_len);
            retval = parse_string(buf, rmsg);
            if (retval < 0)
            {
                continue;
            }
            queue_push_tail(queue, rmsg);
        }
    }

exit:

    if (args->sockfd)
        close(args->sockfd);

    return NULL;
}

void *ipc_send(void *webc_args)
{
    webc_message_t *smsg = (webc_message_t *)malloc(sizeof(webc_message_t));
    int sockfd = socket(AF_UNIX, SOCK_STREAM, 0);
    char buf[SOCK_BUF_SIZE] = {
        0,
    };
    int retval;
    struct sockaddr_un clnt_addr;
    socklen_t addr_len = sizeof(struct sockaddr_un);

    while (!end_webc)
    {
        memset(smsg, 0, sizeof(webc_message_t));
        retval = queue_pop_head(args->queue, smsg);
        if (!retval)
        {
            usleep(10000);
            continue;
        }
        retval = ctrl_msg(smsg);
        if (retval < 0)
        {
            usleep(10000);
            continue;
        }

        memset(buf, 0, SOCK_BUF_SIZE);
        snprintf(buf, sizeof(buf), "%d:%d:%s", smsg->header.command, smsg->header.status, smsg->data);
        memset(&clnt_addr, 0, addr_len);
        memcpy(&clnt_addr, &smsg->clnt_addr, addr_len);
        retval = sendto(sockfd, buf, strlen(buf), 0,
                        (struct sockaddr *)&clnt_addr, addr_len);
        if (retval < 0)
        {
            pv_log_debug("sendto() failed : %s", clnt_addr.sun_path);
        }
    }

    return NULL;
}

int main(int argc, char *argv[])
{
    pthread_t ipc_read_thread;
    pthread_t ipc_send_thread;
    queue = queue_new();

    pthread_create(&ipc_read_thread, NULL, ipc_read, NULL);
    pthread_create(&ipc_send_thread, NULL, ipc_send, NULL);

    pthread_join(ipc_read_thread, NULL);
    pthread_join(ipc_send_thread, NULL);

    return 0;
}